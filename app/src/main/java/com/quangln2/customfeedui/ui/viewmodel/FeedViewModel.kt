package com.quangln2.customfeedui.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.Player
import com.google.gson.Gson
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.controllers.FeedCtrl
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.others.EnumFeedLoadingCode
import com.quangln2.customfeedui.data.models.others.UploadWorkerModel
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.data.models.uimodel.TypeOfPost
import com.quangln2.customfeedui.domain.usecase.DeleteFeedUseCase
import com.quangln2.customfeedui.domain.usecase.GetAllFeedsModifiedUseCase
import com.quangln2.customfeedui.domain.workmanager.UploadService
import com.quangln2.customfeedui.others.callback.GetDataCallback
import com.quangln2.customfeedui.others.utils.DownloadUtils
import com.quangln2.customfeedui.ui.customview.LoadingVideoView
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

    fun getAllFeeds(preloadCache: Boolean = false, onNotChangedData: () -> Unit = {}) {
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

    fun onHandlePlayVideoAndDownloadVideo(firstVisibleItemPosition: Int, context: Context, playVideoWrapper: () -> Unit){
        playVideoWrapper()
        if(firstVisibleItemPosition > 0){
            val item = uploadLists.value?.get(firstVisibleItemPosition - 1)
            item?.apply {
                viewModelScope.launch(Dispatchers.IO) {
                    for (urlObj in resources) {
                        DownloadUtils.downloadResource(urlObj.url, context)
                    }
                }
            }
        }
    }

    fun retrieveAllVisibleItems(firstPartiallyIndex: Int, lastPartiallyIndex: Int, retrieveAllVisibleVideosOnScreen: (List<Int>) -> Unit){
        val indexLists = HashSet<Int>().apply {
            if(firstPartiallyIndex <= lastPartiallyIndex){
                for(i in firstPartiallyIndex..lastPartiallyIndex) add(i)
            } else {
                add(firstPartiallyIndex)
                add(lastPartiallyIndex)
            }
        }.filter { it >= 0 }.toList()
        retrieveAllVisibleVideosOnScreen(indexLists)
    }

    fun putIncomingVideoToQueue(temporaryVideoSequence: MutableList<Pair<Int, Int>>, pauseVideoUtil: () -> Unit){
        Log.v("FeedFragment", "Compare two arrays $temporaryVideoSequence ${FeedCtrl.videoDeque}")
        if(!FeedCtrl.compareDequeWithList(temporaryVideoSequence)){
            if(temporaryVideoSequence.isEmpty()){
                temporaryVideoSequence.addAll(FeedCtrl.videoDeque)
            } else {
                temporaryVideoSequence.clear()
                temporaryVideoSequence.addAll(FeedCtrl.videoDeque)
            }


            val pair = FeedCtrl.peekFirst()
            if(pair.first != -1 && pair.second != -1){
                //Case we have video in queue
                if(FeedCtrl.playingQueue.isEmpty()){
                    //Case 1: No video available in playing queue
                    FeedCtrl.popFirstSafely()
                    FeedCtrl.playingQueue.add(pair)
                } else {
                    //Case 2: We have video in playing queue
                    val (itemIndex, videoIndex) = FeedCtrl.playingQueue.peek()!!
                    if(itemIndex == pair.first && videoIndex == pair.second){
                        //Case 2.1: The video in playing queue is the same as the video in queue
                        FeedCtrl.popFirstSafely()
                    } else {
                        //Case 2.2: The video in playing queue is different from the video in queue
                        Log.v("FeedFragment", "-----------------------")
                        Log.v("FeedFragment", "Different ${FeedCtrl.playingQueue} ${FeedCtrl.videoDeque}")
                        pauseVideoUtil()
                        FeedCtrl.popFirstSafely()
                        FeedCtrl.playingQueue.clear()
                        FeedCtrl.playingQueue.add(pair)
                        Log.v("FeedFragment", "Different ${FeedCtrl.playingQueue} ${FeedCtrl.videoDeque}")
                        Log.v("FeedFragment", "-----------------------")
                        temporaryVideoSequence.clear()
                    }
                }
            } else {
                //Case no video in feeds
                while(FeedCtrl.playingQueue.isNotEmpty()){
                    pauseVideoUtil()
                    FeedCtrl.popFirstSafely()
                }
                temporaryVideoSequence.clear()
            }
        }
        Log.v("FeedFragment", "After processing $temporaryVideoSequence ${FeedCtrl.videoDeque}")
    }

    fun checkLoadingVideoViewIsVisible(view: View, currentViewRect: Rect, videoPair: Pair<Int, Int>){
        val isVisible = if(view is LoadingVideoView){
            view.getLocalVisibleRect(currentViewRect)
            val height = currentViewRect.height()
            val isOutOfBoundsOnTheTop = currentViewRect.bottom < 0 && currentViewRect.top < 0
            val isOutOfBoundsAtTheBottom =
                currentViewRect.top >= ConstantSetup.PHONE_HEIGHT && currentViewRect.bottom >= ConstantSetup.PHONE_HEIGHT
            if (isOutOfBoundsAtTheBottom || isOutOfBoundsOnTheTop) {
                false
            } else {
                val tmp = view.height
                val percents = height * 100 / tmp
                percents >= 50
            }
        } else false
        if(isVisible){
            FeedCtrl.addToLast(videoPair.first, videoPair.second)
        }
    }
    fun onPlaybackStateEnded(playbackState: Int, temporaryVideoSequence: MutableList<Pair<Int, Int>>, onEndPlayVideo: () -> Unit, onPlayVideo: () -> Unit){
        if (playbackState == Player.STATE_ENDED) {
            onEndPlayVideo()
            if(FeedCtrl.videoDeque.isNotEmpty()){
                val playedPair = FeedCtrl.playingQueue.remove()
                val index = FeedCtrl.videoDeque.indexOfFirst { it == playedPair }
                if(index != -1){
                    for(i in 0 .. index){
                        FeedCtrl.videoDeque.removeFirst()
                    }
                }
                val pair = FeedCtrl.videoDeque.peek()
                if(pair != null){
                    FeedCtrl.playingQueue.add(pair)
                    onPlayVideo()
                } else {
                    temporaryVideoSequence.forEach{
                        FeedCtrl.videoDeque.add(it)
                    }
                    val anotherPair = FeedCtrl.videoDeque.peek()
                    if(anotherPair != null){
                        FeedCtrl.playingQueue.add(anotherPair)
                        onPlayVideo()
                    }

                }
            }
        }
    }

}