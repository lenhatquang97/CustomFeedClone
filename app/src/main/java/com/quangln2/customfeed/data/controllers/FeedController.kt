package com.quangln2.customfeed.data.controllers

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import java.util.*

data class VideoPlayed(
    val itemPosition: Int,
    val index: Int,
)

object FeedController {
    var isLoading = MutableLiveData<Boolean>().apply { value = false }

    var videoQueue: Queue<VideoPlayed> = LinkedList()
    fun safeRemoveFromQueue() {
        if (videoQueue.size > 0) {
            videoQueue.remove()
        }
    }

    fun isViewAddedToQueue(view: View, itemPosition: Int, i: Int): Boolean {
        val isAvailable = videoQueue.find { it.itemPosition == itemPosition && it.index == i }
        if (view is LoadingVideoView && isAvailable == null) {
            videoQueue.add(VideoPlayed(itemPosition, i))
            return true
        }
        return false
    }

    fun peekVideoQueue(): Pair<Int?, Int?> {
        return Pair(videoQueue.peek()?.itemPosition, videoQueue.peek()?.index)
    }

    fun popVideoQueue(): Pair<Int?, Int?> {
        val pair = Pair(videoQueue.peek()?.itemPosition, videoQueue.peek()?.index)
        safeRemoveFromQueue()
        return pair
    }
}