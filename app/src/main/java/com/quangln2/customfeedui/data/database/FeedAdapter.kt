package com.quangln2.customfeedui.data.database

import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.datamodel.OfflineResource
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.others.utils.DownloadUtils

fun convertFromUploadPostToMyPost(uploadPost: UploadPost, oldPost: List<MyPost>): MyPost {
    val myPost = MyPost().apply {
        feedId = uploadPost.feedId
        avatar = uploadPost.avatar
        name = uploadPost.name
        caption = uploadPost.caption
        createdTime = uploadPost.createdTime
        firstWidth = uploadPost.firstWidth
        firstHeight = uploadPost.firstHeight
    }

    /* Note: In MongoDB, no items on array or in any attributes will return null :) */
    if (uploadPost.imagesAndVideos !=null && uploadPost.imagesAndVideos.size > 0) {
        for (i in 0 until uploadPost.imagesAndVideos.size) {
            val value = uploadPost.imagesAndVideos[i]
            val (fileSize, _) = DownloadUtils.fileSizeFromInternet(value)
            val anotherSize = oldPost.find { it.feedId == uploadPost.feedId }?.resources?.find { it.url == value }?.size
            val actualFileSize = if (anotherSize != null && anotherSize > fileSize) anotherSize else fileSize
            myPost.resources.add(OfflineResource(value, actualFileSize, 0L))
        }
    }

    return myPost
}
