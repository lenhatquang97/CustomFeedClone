package com.quangln2.customfeed.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quangln2.customfeed.data.database.convertFromUploadPostToMyPost
import com.quangln2.customfeed.data.models.MyPost
import com.quangln2.customfeed.data.models.UploadPost
import com.quangln2.customfeed.domain.*
import com.quangln2.customfeed.others.utils.DownloadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedViewModel(
    val uploadPostUseCase: UploadPostUseCase,
    val getAllFeedsUseCase: GetAllFeedsUseCase,
    val deleteFeedUseCase: DeleteFeedUseCase,
    val uploadMultipartBuilderUseCase: UploadMultipartBuilderUseCase,
    val insertDatabaseUseCase: InsertDatabaseUseCase,
    val updateDatabaseUseCase: UpdateDatabaseUseCase,
    val deleteDatabaseUseCase: DeleteDatabaseUseCase,
    val getAllInDatabaseUseCase: GetAllInDatabaseUseCase
) : ViewModel() {
    var _uriLists = MutableLiveData<MutableList<Uri>>().apply { value = mutableListOf() }
    val uriLists: LiveData<MutableList<Uri>> = _uriLists

    private var _uploadLists = MutableLiveData<MutableList<MyPost>>().apply { value = mutableListOf() }
    val uploadLists: LiveData<MutableList<MyPost>> = _uploadLists

    private var _isUploading = MutableLiveData<Boolean>().apply { value = false }
    val isUploading: LiveData<Boolean> = _isUploading

    private var _feedLoadingCode = MutableLiveData<Int>().apply { value = 0 }
    val feedLoadingCode: LiveData<Int> = _feedLoadingCode

    private var _feedUploadingCode = MutableLiveData<Int>().apply { value = 0 }
    val feedUploadingCode: LiveData<Int> = _feedUploadingCode




    fun uploadFiles(requestBody: List<MultipartBody.Part>, context: Context) {
        _isUploading.value = true
        uploadPostUseCase(requestBody).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    println(response.body())
                    Log.d("UploadFile", "Success")
                    Toast.makeText(context, "Upload Success", Toast.LENGTH_SHORT).show()
                }
                _feedUploadingCode.value = response.code()
                _isUploading.value = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("UploadFile", "Failure")
                println(t.cause?.message)
                _feedUploadingCode.value = -1
                _isUploading.value = false
            }

        })
    }

    fun downloadAllResourcesWithUpdate(context: Context, uploadPosts: List<UploadPost>){
        viewModelScope.launch(Dispatchers.IO){
            for (item in uploadPosts){
                if(item.imagesAndVideos == null) continue
                for(urlObj in item.imagesAndVideos){
                    DownloadUtils.downloadResource(urlObj, context)
                }
            }

        }
    }

    private fun loadCache(){
        val ls = mutableListOf<MyPost>()
        ls.add(MyPost().copy(feedId = "none"))
        viewModelScope.launch(Dispatchers.Main){
            val offlinePosts = getAllInDatabaseUseCase().last()
            for(item in offlinePosts){
                ls.add(item)
            }
            _feedLoadingCode.value = -1
            val firstItemImport = ls.first()
            val remainingItems = ls.filter { it.feedId != firstItemImport.feedId }
            val sortedList = remainingItems.sortedWith(
                compareByDescending<MyPost> { it.createdTime.toLong() }
            ) .toMutableList()
            val newLists = mutableListOf<MyPost>()
            newLists.add(firstItemImport)
            newLists.addAll(sortedList)
            _uploadLists.value = newLists.toMutableList()

        }
    }

    fun getAllFeeds(context: Context) {
        getAllFeedsUseCase().enqueue(object : Callback<MutableList<UploadPost>> {
            override fun onResponse(call: Call<MutableList<UploadPost>>, response: Response<MutableList<UploadPost>>) {
                loadCache()

                val ls = mutableListOf<MyPost>()
                if (response.code() == 200) {
                    val body = response.body()
                    ls.add(MyPost().copy(feedId = "none"))
                    viewModelScope.launch(Dispatchers.IO){
                        val offlinePosts = getAllInDatabaseUseCase().first()
                        if(body != null){
                            for(i in 0 until body.size){
                                ls.add(convertFromUploadPostToMyPost(body[i], offlinePosts))
                            }
                            _feedLoadingCode.postValue(response.code())
                            _uploadLists.postValue(ls.toMutableList())
                            withContext(Dispatchers.IO){
                                val listExcludedWithNone = ls.filter { it.feedId != "none" }
                                for (item in listExcludedWithNone) insertDatabaseUseCase(item)
                                downloadAllResourcesWithUpdate(context, body)
                            }
                        } else{
                            _feedLoadingCode.postValue(response.code())
                            _uploadLists.postValue(ls.toMutableList())
                        }
                    }
                } else {
                    loadCache()
                }
            }

            override fun onFailure(call: Call<MutableList<UploadPost>>, t: Throwable) {
                Log.d("GetAllFeeds", "Failure")
                println(t.cause?.message)
                loadCache()
            }

        })
    }

    fun getFeedItem(feedId: String): MyPost {
        val ls = _uploadLists.value
        if (ls != null) {
            val indexOfFirst = ls.indexOfFirst { it.feedId == feedId }
            return ls[indexOfFirst]
        }
        return MyPost().copy(feedId = "none")

    }

    fun deleteFeed(id: String) {
        deleteFeedUseCase(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    viewModelScope.launch(Dispatchers.IO){
                        deleteDatabaseUseCase(id)
                    }
                    val ls = uploadLists.value
                    val filteredList = ls?.filter { it.feedId != id }

                    _uploadLists.value = filteredList?.toMutableList()

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("DeleteFeed", "Failure")
                println(t.cause?.message)
            }

        })
    }

    fun uploadMultipartBuilder(
        caption: String,
        uriLists: LiveData<MutableList<Uri>>,
        context: Context
    ): List<MultipartBody.Part> {
        return uploadMultipartBuilderUseCase(caption, uriLists, context)
    }
}