package com.quangln2.customfeed.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import com.quangln2.customfeed.data.models.datamodel.OfflineResource
import com.quangln2.customfeed.data.models.datamodel.UploadPost
import com.quangln2.customfeed.data.models.uimodel.MyPostRender
import com.quangln2.customfeed.data.models.uimodel.TypeOfPost
import com.quangln2.customfeed.domain.*
import com.quangln2.customfeed.domain.workmanager.UploadFileWorker
import com.quangln2.customfeed.others.utils.DownloadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedViewModel(
    val getAllFeedsUseCase: GetAllFeedsUseCase,
    val deleteFeedUseCase: DeleteFeedUseCase,
    val insertDatabaseUseCase: InsertDatabaseUseCase,
    val deleteDatabaseUseCase: DeleteDatabaseUseCase,
    val getAllInDatabaseUseCase: GetAllInDatabaseUseCase,
    val getFeedByIdUseCase: GetFeedByIdUseCase
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
        //Handle no posts
        if (caption.isEmpty() && uriLists.size == 0) {
            Toast.makeText(context, "Please add some content", Toast.LENGTH_SHORT).show()
            return
        }



        FeedController.isLoading.value = 1

        val uriStringLists = uriLists.map { it.toString() }
        val uploadWorkerModel = UploadWorkerModel(caption, uriStringLists)

        val jsonString = Gson().toJson(uploadWorkerModel)
        val inputData = Data.Builder().putString("jsonString", jsonString).build()

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(UploadFileWorker::class.java)
            .setInputData(inputData)
            .build()

        val workManager = WorkManager.getInstance(context)
            .beginUniqueWork("ForegroundWorker", ExistingWorkPolicy.APPEND_OR_REPLACE, oneTimeWorkRequest)
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
        viewModelScope.launch(Dispatchers.IO) {
            val offlinePosts = getAllInDatabaseUseCase()
            for (item in offlinePosts) {
                ls.add(item)
            }
            _feedLoadingCode.postValue(-1)
            _uploadLists.postValue(ls.toMutableList())
        }

    }

    fun getAllFeedsWithPreloadCache(context: Context) {
        loadCache()
        getAllFeeds(context)
    }

    fun getAllFeeds(context: Context) {
        getAllFeedsUseCase().enqueue(object : Callback<MutableList<UploadPost>> {
            override fun onResponse(call: Call<MutableList<UploadPost>>, response: Response<MutableList<UploadPost>>) {
                if (response.code() == 200) {
                    val ls = mutableListOf<MyPost>()
                    val body = response.body()
                    viewModelScope.launch(Dispatchers.IO) {
                        val offlinePosts = getAllInDatabaseUseCase()
                        if (body != null) {
                            body.forEach {
                                val itemConverted = convertFromUploadPostToMyPost(it, offlinePosts)
                                ls.add(itemConverted)
                            }
                            withContext(Dispatchers.IO) {
                                ls.forEach {
                                    insertDatabaseUseCase(it)
                                }
                            }
                            Log.d("FeedViewModel", "ls: ${ls.joinToString { it.caption }}")
                        }
                        _feedLoadingCode.postValue(response.code())
                        _uploadLists.postValue(ls.toMutableList())

                    }
                }
            }

            override fun onFailure(call: Call<MutableList<UploadPost>>, t: Throwable) {
                Log.d("GetAllFeeds", "Failure ${t.cause?.message}")
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
        return MyPostRender.convertMyPostToMyPostRender(MyPost(), TypeOfPost.ADD_NEW_POST)

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
                Log.d("DeleteFeed", "Failure ${t.cause?.message}")
            }

        })
    }

    fun downloadResourceWithId(resources: MutableList<OfflineResource>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            for (urlObj in resources) {
                DownloadUtils.downloadResource(urlObj.url, context)
            }
        }
    }
}