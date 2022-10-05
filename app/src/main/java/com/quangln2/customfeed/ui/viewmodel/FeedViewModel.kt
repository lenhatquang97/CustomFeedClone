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

    fun uploadFiles(requestBody: List<MultipartBody.Part>, context: Context) {
        _isUploading.value = true
        uploadPostUseCase(requestBody).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    println(response.body())
                    Log.d("UploadFile", "Success")
                    Toast.makeText(context, "Upload Success", Toast.LENGTH_SHORT).show()
                    _isUploading.value = false
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("UploadFile", "Failure")
                println(t.cause?.message)
                _isUploading.value = false
            }

        })
    }

    fun downloadAllResourcesWithUpdate(context: Context, uploadPosts: List<UploadPost>){
        viewModelScope.launch(Dispatchers.IO){
            for (item in uploadPosts){
                for(urlObj in item.imagesAndVideos){
                    DownloadUtils.downloadResource(urlObj, context)
                }
            }

        }
    }

    fun getAllFeeds(context: Context) {
        getAllFeedsUseCase().enqueue(object : Callback<MutableList<UploadPost>> {
            override fun onResponse(call: Call<MutableList<UploadPost>>, response: Response<MutableList<UploadPost>>) {
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
                            _uploadLists.postValue(ls.toMutableList())

                            withContext(Dispatchers.IO){
                                val listExcludedWithNone = ls.filter { it.feedId != "none" }
                                for (item in listExcludedWithNone) insertDatabaseUseCase(item)
                                downloadAllResourcesWithUpdate(context, body)
                            }
                        }
                    }
                } else {
                    ls.add(MyPost().copy(feedId = "none"))
                    viewModelScope.launch(Dispatchers.Main){
                        val offlinePosts = getAllInDatabaseUseCase().first()
                        for(item in offlinePosts){
                            ls.add(item)
                        }
                        _uploadLists.value = ls.toMutableList()
                    }
                }
            }

            override fun onFailure(call: Call<MutableList<UploadPost>>, t: Throwable) {
                Log.d("GetAllFeeds", "Failure")
                println(t.cause?.message)
                val ls = mutableListOf<MyPost>()
                ls.add(MyPost().copy(feedId = "none"))
                viewModelScope.launch(Dispatchers.Main){
                    val offlinePosts = getAllInDatabaseUseCase().first()
                    for(item in offlinePosts){
                        ls.add(item)
                    }
                    _uploadLists.value = ls.toMutableList()
                }


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
                    val ls = uploadLists.value
                    ls?.removeIf { it.feedId == id }
                    _uploadLists.value = ls?.toMutableList()
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