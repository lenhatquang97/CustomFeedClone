package com.quangln2.customfeed.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.gson.Gson
import com.quangln2.customfeed.data.controllers.FeedController
import com.quangln2.customfeed.data.database.convertFromUploadPostToMyPost
import com.quangln2.customfeed.data.models.UploadWorkerModel
import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.data.models.datamodel.UploadPost
import com.quangln2.customfeed.data.models.uimodel.MyPostRender
import com.quangln2.customfeed.domain.*
import com.quangln2.customfeed.domain.workmanager.UploadFileWorker
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



    fun uploadFiles(caption: String, uriLists: MutableList<Uri>, context: Context) {
        FeedController.isLoading.value = true

        val uriStringLists = uriLists.map { it.toString() }
        val uploadWorkerModel = UploadWorkerModel(caption, uriStringLists)

        val jsonString = Gson().toJson(uploadWorkerModel)
        val inputData = Data.Builder().putString("jsonString", jsonString).build()

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(UploadFileWorker::class.java)
            .setInputData(inputData)
            .build()

        val workManager = WorkManager.getInstance(context).beginUniqueWork("ForegroundWorker", ExistingWorkPolicy.APPEND_OR_REPLACE, oneTimeWorkRequest)
        workManager.enqueue()
    }

    fun downloadAllResourcesWithUpdate(context: Context, uploadPosts: List<UploadPost>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (item in uploadPosts) {
                if (item.imagesAndVideos == null) continue
                for (urlObj in item.imagesAndVideos) {
                    DownloadUtils.downloadResource(urlObj, context)
                }
            }

        }
    }

    private fun loadCache() {
        val ls = mutableListOf<MyPost>()
        viewModelScope.launch(Dispatchers.Main) {
            val offlinePosts = getAllInDatabaseUseCase().last()
            for (item in offlinePosts) {
                ls.add(item)
            }
            _feedLoadingCode.value = -1
            _uploadLists.value = ls.toMutableList()

        }
    }

    fun getAllFeeds(context: Context) {
        getAllFeedsUseCase().enqueue(object : Callback<MutableList<UploadPost>> {
            override fun onResponse(call: Call<MutableList<UploadPost>>, response: Response<MutableList<UploadPost>>) {
                loadCache()
                if (response.code() == 200) {
                    val ls = mutableListOf<MyPost>()
                    val body = response.body()
                    viewModelScope.launch(Dispatchers.IO) {
                        val offlinePosts = getAllInDatabaseUseCase().first()
                        if (body != null) {
                            body.forEach {
                                val itemConverted = convertFromUploadPostToMyPost(it, offlinePosts)
                                ls.add(itemConverted)
                            }
                            println("ls: ${ls.joinToString { it.caption }}")
                            withContext(Dispatchers.Main){
                                _feedLoadingCode.value = response.code()
                                _uploadLists.value = ls.toMutableList()
                            }

                            withContext(Dispatchers.IO) {
                                ls.forEach { insertDatabaseUseCase(it) }
                                downloadAllResourcesWithUpdate(context, body)
                            }
                        } else {
                            _feedLoadingCode.postValue(response.code())
                            _uploadLists.postValue(ls.toMutableList())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MutableList<UploadPost>>, t: Throwable) {
                Log.d("GetAllFeeds", "Failure")
                println(t.cause?.message)
                loadCache()
            }

        })
    }

    fun getFeedItem(feedId: String): MyPostRender {
        val ls = _uploadLists.value
        if (ls != null) {
            val indexOfFirst = ls.indexOfFirst { it.feedId == feedId }
            return MyPostRender.convertMyPostToMyPostRender(ls[indexOfFirst])
        }
        return MyPostRender.convertMyPostToMyPostRender(MyPost(), "AddNewPost")

    }

    fun deleteFeed(id: String) {
        deleteFeedUseCase(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    viewModelScope.launch(Dispatchers.IO) {
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
        uriLists: MutableList<Uri>,
        context: Context
    ): List<MultipartBody.Part> {
        return uploadMultipartBuilderUseCase(caption, uriLists, context)
    }
}