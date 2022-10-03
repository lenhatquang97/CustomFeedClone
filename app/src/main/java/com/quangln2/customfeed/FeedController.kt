package com.quangln2.customfeed

import android.view.View
import com.quangln2.customfeed.customview.LoadingVideoView
import java.util.*

data class VideoPlayed(
    val itemPosition: Int,
    val index: Int,
)

object FeedController {
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
}