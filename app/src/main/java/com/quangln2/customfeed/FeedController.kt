package com.quangln2.customfeed

import java.util.*

data class VideoPlayed(
    val itemPosition: Int,
    val index: Int,
)

object FeedController {
    var videoQueue: Queue<VideoPlayed> = LinkedList()
}