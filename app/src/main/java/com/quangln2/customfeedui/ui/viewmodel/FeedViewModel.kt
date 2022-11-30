package com.quangln2.customfeedui.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.Player
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.controllers.FeedCtrl
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.others.EnumFeedLoadingCode
import com.quangln2.customfeedui.data.models.others.EnumFeedSplashScreenState
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.domain.usecase.DeleteFeedUseCase
import com.quangln2.customfeedui.domain.usecase.GetAllFeedsModifiedUseCase
import com.quangln2.customfeedui.domain.workmanager.UploadService
import com.quangln2.customfeedui.others.utils.DownloadUtils
import com.quangln2.customfeedui.ui.customview.LoadingVideoView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class FeedViewModel(
    val deleteFeedUseCase: DeleteFeedUseCase,
    val getAllFeedsModifiedUseCase: GetAllFeedsModifiedUseCase
) : ViewModel() {
    private var _uploadLists = MutableLiveData<MutableList<MyPost>>()
    val uploadLists: LiveData<MutableList<MyPost>> = _uploadLists

    private var _feedLoadingCode = MutableLiveData<Int>()
    private val feedLoadingCode: LiveData<Int> = _feedLoadingCode

    private val temporaryVideoSequence by lazy { mutableListOf<Pair<Int, Int>>() }

    val noPostIdVisibility = MutableLiveData<Boolean>()
    val retryButtonVisibility = MutableLiveData<Boolean>()
    val allFeedsVisibility = MutableLiveData<Boolean>()
    val isRefreshingLoadState = MutableLiveData<Boolean>()
    val isGoingToUploadState = MutableLiveData<Boolean>()

    init {
        _uploadLists.apply { value = mutableListOf() }
        _feedLoadingCode.apply { value = EnumFeedLoadingCode.INITIAL.value }
        noPostIdVisibility.apply { value = false }
        retryButtonVisibility.apply { value = false }
        allFeedsVisibility.apply { value = false }
        isRefreshingLoadState.apply { value = false }
        isGoingToUploadState.apply { value = false }
    }

    fun getAllFeeds(preloadCache: Boolean = false){
        viewModelScope.launch(Dispatchers.IO) {
            getAllFeedsModifiedUseCase(preloadCache).collect{
                if(it.contains("onGetFeedLoadingCode")){
                    val commandParse = it.split(" ")
                    _feedLoadingCode.postValue(commandParse[1].toInt())
                } else {
                    val jsonParse = MyPost.jsonStringToList(it).toMutableList()
                    _uploadLists.postValue(jsonParse)
                    isRefreshingLoadState.postValue(false)
                }
            }
        }
    }

    fun deleteFeed(id: String, context: Context) {
        val oldLists = uploadLists.value
        oldLists?.apply {
            viewModelScope.launch(Dispatchers.IO) {
                deleteFeedUseCase(id, oldLists, context).collect{
                    val jsonParse = MyPost.jsonStringToList(it).toMutableList()
                    _uploadLists.postValue(jsonParse)
                }
            }

        }
    }

    fun onHandlePlayVideoAndDownloadVideo(firstVisibleItemPosition: Int, context: Context): Flow<String> = flow{
        emit("playVideoWrapper")
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

    fun retrieveAllVisibleItems(firstPartiallyIndex: Int, lastPartiallyIndex: Int): List<Int>{
        val firstActualIndex = if(firstPartiallyIndex < 0) 0 else firstPartiallyIndex

        return HashSet<Int>().apply {
            if (firstActualIndex <= lastPartiallyIndex) {
                for (i in firstActualIndex..lastPartiallyIndex) add(i)
            } else {
                add(firstActualIndex)
                add(lastPartiallyIndex)
            }
        }.filter { it >= 0 }.toList()
    }

    fun putIncomingVideoToQueue(): Flow<String> = flow{
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
                        emit("pauseVideoUtilCustom")
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
                    emit("pauseVideoUtilCustom")
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
    fun onPlaybackStateEnded(playbackState: Int): Flow<String> =  flow{
        val onEndPlayVideo = "onEndPlayVideo"
        val onPlayVideo = "onPlayVideo"

        if (playbackState == Player.STATE_ENDED) {
            emit(onEndPlayVideo)
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
                    emit(onPlayVideo)
                } else {
                    temporaryVideoSequence.forEach{
                        FeedCtrl.videoDeque.add(it)
                    }
                    val anotherPair = FeedCtrl.videoDeque.peek()
                    if(anotherPair != null){
                        FeedCtrl.playingQueue.add(anotherPair)
                        emit(onPlayVideo)
                    }

                }
            }
        }
    }

    fun stopUploadService(context: Context){
        val intent = Intent(context, UploadService::class.java)
        context.stopService(intent)
    }
    fun startUploadService(context: Context){
        val intent = Intent(context, UploadService::class.java)
        context.startService(intent)
    }

    fun manageUploadList(listOfPostRender: List<MyPostRender>){
        noPostIdVisibility.value = false
        retryButtonVisibility.value = true
        val feedLoadingCode = feedLoadingCode.value
        val emptyFeedCondition = listOfPostRender.size == 1 && feedLoadingCode == EnumFeedLoadingCode.SUCCESS.value
        val havePostCondition = listOfPostRender.size > 1
        val condition = emptyFeedCondition || havePostCondition
        if(condition){
            noPostIdVisibility.value = false
            retryButtonVisibility.value = false
            allFeedsVisibility.value = true
            isRefreshingLoadState.value = false
        } else {
            noPostIdVisibility.value = false
            allFeedsVisibility.value = false
            retryButtonVisibility.value = true
        }
    }

    fun manageUploadState(state: Int, context: Context){
        isGoingToUploadState.value = state == EnumFeedSplashScreenState.LOADING.value
        if (state == EnumFeedSplashScreenState.COMPLETE.value) {
            isRefreshingLoadState.value = true
            isGoingToUploadState.value = false
            viewModelScope.launch(Dispatchers.Main){
                getAllFeeds()
            }
            stopUploadService(context)
            FeedCtrl.isLoadingToUpload.value = EnumFeedSplashScreenState.UNDEFINED.value
        }
    }

    fun onHandleRetryButton(){
        noPostIdVisibility.value = true
        retryButtonVisibility.value = false
        viewModelScope.launch(Dispatchers.Main){
            getAllFeeds(preloadCache = true)
        }
    }

    fun onHandleSwipeRefresh(){
        viewModelScope.launch(Dispatchers.Main) {
            getAllFeeds()
            isRefreshingLoadState.value = false
        }
    }
}