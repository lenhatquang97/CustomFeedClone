package com.quangln2.customfeed.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.quangln2.customfeed.data.database.convertFromUploadPostToMyPost
import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.data.models.datamodel.OfflineResource
import com.quangln2.customfeed.data.models.datamodel.UploadPost
import com.quangln2.customfeed.data.models.others.EnumFeedLoadingCode
import com.quangln2.customfeed.data.models.others.UploadWorkerModel
import com.quangln2.customfeed.data.models.uimodel.MyPostRender
import com.quangln2.customfeed.data.models.uimodel.TypeOfPost
import com.quangln2.customfeed.domain.usecase.*
import com.quangln2.customfeed.domain.workmanager.UploadService
import com.quangln2.customfeed.others.utils.DownloadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedViewModel(
    val getAllFeedsUseCase: GetAllFeedsUseCase,
    val deleteFeedUseCase: DeleteFeedUseCase,
    val insertDatabaseUseCase: InsertDatabaseUseCase,
    val deleteDatabaseUseCase: DeleteDatabaseUseCase,
    val getAllInDatabaseUseCase: GetAllInDatabaseUseCase
) : ViewModel() {
    var _uriLists = MutableLiveData<MutableList<Uri>>().apply { value = mutableListOf() }
    val uriLists: LiveData<MutableList<Uri>> = _uriLists

    private var _uploadLists = MutableLiveData<MutableList<MyPost>>().apply { value = mutableListOf() }
    val uploadLists: LiveData<MutableList<MyPost>> = _uploadLists

    private var _feedLoadingCode = MutableLiveData<Int>().apply { value = EnumFeedLoadingCode.INITIAL.value }
    val feedLoadingCode: LiveData<Int> = _feedLoadingCode


    fun uploadFiles(caption: String, uriLists: MutableList<Uri>, context: Context) {
        val uriStringLists = uriLists.map { it.toString() }
        val uploadWorkerModel = UploadWorkerModel(caption, uriStringLists)
        val jsonString = Gson().toJson(uploadWorkerModel)
        val intent = Intent(context, UploadService::class.java)
        intent.putExtra("jsonString", jsonString)
        context.startService(intent)

    }

    private fun loadCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val offlinePosts = getAllInDatabaseUseCase()
            _feedLoadingCode.postValue(EnumFeedLoadingCode.OFFLINE.value)
            _uploadLists.postValue(offlinePosts.toMutableList())
        }

    }

    fun getAllFeedsWithPreloadCache() {
        loadCache()
        getAllFeeds()
    }

    fun getAllFeeds() {
        getAllFeedsUseCase().enqueue(object : Callback<MutableList<UploadPost>> {
            override fun onResponse(call: Call<MutableList<UploadPost>>, response: Response<MutableList<UploadPost>>) {
                if (response.code() == 200) {
                    val ls = mutableListOf<MyPost>()
                    val body = response.body()
                    val deletedFeeds = mutableListOf<MyPost>()

                    viewModelScope.launch(Dispatchers.IO) {
                        val offlinePosts = getAllInDatabaseUseCase()
                        if (body != null) {
                            //add to offline feeds
                            body.forEach {
                                val itemConverted = convertFromUploadPostToMyPost(it, offlinePosts)
                                ls.add(itemConverted)
                            }

                            //find in offline feeds if there are no online posts in online database
                            offlinePosts.forEach {
                                val filterId = body.find { item -> item.feedId == it.feedId }
                                if (filterId == null) {
                                    deletedFeeds.add(it)
                                }
                            }
                            //deleted first
                            deletedFeeds.forEach {
                                deleteDatabaseUseCase(it.feedId)
                            }

                            val availableItems =
                                ls.filter { item -> deletedFeeds.find { it.feedId == item.feedId } == null }
                            availableItems.forEach {
                                insertDatabaseUseCase(it)
                            }

                        }
                        //get available items but not in deleted feeds
                        val availableItems =
                            ls.filter { item -> deletedFeeds.find { it.feedId == item.feedId } == null }
                        _feedLoadingCode.postValue(response.code())
                        _uploadLists.postValue(availableItems.toMutableList())
                    }
                } else loadCache()

            }

            override fun onFailure(call: Call<MutableList<UploadPost>>, t: Throwable) {
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

    fun deleteFeed(id: String, context: Context) {
        deleteFeedUseCase(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.errorBody() == null && response.code() == 200) {
                    viewModelScope.launch(Dispatchers.IO) {
                        deleteDatabaseUseCase(id)
                    }
                    val ls = uploadLists.value
                    val filteredList = ls?.filter { it.feedId != id }
                    _uploadLists.value = filteredList?.toMutableList()
                    Toast.makeText(context, "Delete successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
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