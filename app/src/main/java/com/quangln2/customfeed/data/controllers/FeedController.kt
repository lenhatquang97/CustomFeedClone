package com.quangln2.customfeed.data.controllers

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import java.util.*

data class VideoPlayed(
    val itemPosition: Int,
    val index: Int
)

object FeedController {
    var isLoading = MutableLiveData<Int>().apply { value = -1 }
    private val videoQueue: Queue<VideoPlayed> = LinkedList()
    private val playedVideos = mutableMapOf<Int, Int>()

    fun isViewAddedToQueue(view: View, itemPosition: Int, i: Int): Boolean {
        val (a, b) = peekVideoQueue()
        if (view is LoadingVideoView && ((a == null && b == null) || (a != null && b != null && a < itemPosition && b < i))) {
            videoQueue.add(VideoPlayed(itemPosition, i))
            return true
        }
        return false
    }

    fun videoQueueSize() = videoQueue.size
    fun addedToQueue(itemPosition: Int, i: Int) = videoQueue.add(VideoPlayed(itemPosition, i))
    fun peekVideoQueue(): Pair<Int?, Int?> = Pair(videoQueue.peek()?.itemPosition, videoQueue.peek()?.index)
    fun popVideoQueue(): Pair<Int?, Int?> {
        val pair = Pair(videoQueue.peek()?.itemPosition, videoQueue.peek()?.index)
        safeRemoveFromQueue()
        return pair
    }
    fun safeRemoveFromQueue(){ if (videoQueue.size > 0) videoQueue.remove() }

    fun addedToPlayedVideos(itemPosition: Int, i: Int){
        playedVideos[itemPosition] = i
    }
    fun getPlayedVideos(itemPosition: Int): Int{
        return playedVideos[itemPosition] ?: -1
    }


}