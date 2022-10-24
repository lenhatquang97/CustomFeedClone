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
    var videoQueue: Queue<VideoPlayed> = LinkedList()

    fun safeRemoveFromQueue() {
        if (videoQueue.size > 0) {
            videoQueue.remove()
        }
    }
    fun removeAllFromQueue() {
        videoQueue.clear()
    }

    fun isViewAddedToQueue(view: View, itemPosition: Int, i: Int): Boolean {
        val (a, b) = peekVideoQueue()
        if (view is LoadingVideoView && ((a == null && b == null) || (a!= null && b!= null && a < itemPosition && b < i))) {
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