package com.quangln2.customfeedui.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.datamodel.OfflineResource
import com.quangln2.customfeedui.data.models.others.EnumFeedLoadingCode
import com.quangln2.customfeedui.data.models.others.UploadWorkerModel
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.data.models.uimodel.TypeOfPost
import com.quangln2.customfeedui.domain.usecase.DeleteFeedUseCase
import com.quangln2.customfeedui.domain.usecase.GetAllFeedsModifiedUseCase
import com.quangln2.customfeedui.domain.workmanager.UploadService
import com.quangln2.customfeedui.others.callback.GetDataCallback
import com.quangln2.customfeedui.others.utils.DownloadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeedViewModel(
    val deleteFeedUseCase: DeleteFeedUseCase,
    val getAllFeedsModifiedUseCase: GetAllFeedsModifiedUseCase
) : ViewModel() {
    private var _uriLists = MutableLiveData<MutableList<Uri>>().apply { value = mutableListOf() }
    val uriLists: LiveData<MutableList<Uri>> = _uriLists

    private var _uploadLists = MutableLiveData<MutableList<MyPost>>().apply { value = mutableListOf() }
    val uploadLists: LiveData<MutableList<MyPost>> = _uploadLists

    private var _feedLoadingCode = MutableLiveData<Int>().apply { value = EnumFeedLoadingCode.INITIAL.value }
    val feedLoadingCode: LiveData<Int> = _feedLoadingCode

    var feedVideoItemPlaying = Pair(-1, -1)

    private val onTakeData = object : GetDataCallback{
        override fun onGetFeedLoadingCode(loadingCode: Int) {
            _feedLoadingCode.value = loadingCode
        }

        override fun onGetUploadList(postList: List<MyPost>) {
            _uploadLists.postValue(postList.toMutableList())
        }
    }

    fun clearImageAndVideoGrid() = _uriLists.value?.clear()
    fun addImageAndVideoGridInBackground(ls: MutableList<Uri>?) = _uriLists.postValue(ls?.toMutableList())

    fun uploadFiles(caption: String, uriLists: MutableList<Uri>, context: Context) {
        val uriStringLists = uriLists.map { it.toString() }
        val uploadWorkerModel = UploadWorkerModel(caption, uriStringLists)
        val jsonString = Gson().toJson(uploadWorkerModel)
        val intent = Intent(context, UploadService::class.java)
        intent.putExtra("jsonString", jsonString)
        context.startService(intent)

    }

    fun getAllFeeds(onNotChangedData: () -> Unit = {}, preloadCache: Boolean = false) {
        getAllFeedsModifiedUseCase(onTakeData, onNotChangedData, viewModelScope, preloadCache)
    }

    fun getFeedItem(feedId: String): MyPostRender {
        val ls = _uploadLists.value
        ls?.apply {
            val indexOfFirst = ls.indexOfFirst { it.feedId == feedId }
            return MyPostRender.convertMyPostToMyPostRender(ls[indexOfFirst])
        }
        return MyPostRender.convertMyPostToMyPostRender(MyPost(), TypeOfPost.ADD_NEW_POST)

    }

    fun deleteFeed(id: String, context: Context) {
        val oldLists = uploadLists.value
        oldLists?.apply {
            deleteFeedUseCase(id, onTakeData, oldLists, viewModelScope, context)
        }
    }

    fun downloadResourceWithId(resources: MutableList<OfflineResource>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            for (urlObj in resources) {
                DownloadUtils.downloadResource(urlObj.url, context)
            }
        }
    }

}