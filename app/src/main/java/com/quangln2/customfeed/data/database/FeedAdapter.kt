package com.quangln2.customfeed.data.database

import com.quangln2.customfeed.data.models.datamodel.DownloadStatus
import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.data.models.datamodel.OfflineResource
import com.quangln2.customfeed.data.models.datamodel.UploadPost
import com.quangln2.customfeed.others.utils.DownloadUtils

fun convertFromUploadPostToMyPost(uploadPost: UploadPost, oldPost: List<MyPost>): MyPost {
    val myPost = MyPost()
    myPost.feedId = uploadPost.feedId
    myPost.avatar = uploadPost.avatar
    myPost.name = uploadPost.name
    myPost.caption = uploadPost.caption
    myPost.createdTime = uploadPost.createdTime
    myPost.firstWidth = uploadPost.firstWidth
    myPost.firstHeight = uploadPost.firstHeight

    if (uploadPost.imagesAndVideos != null && uploadPost.imagesAndVideos.size > 0) {
        for (i in 0 until uploadPost.imagesAndVideos.size) {
            val value = uploadPost.imagesAndVideos[i]
            val (fileSize, _) = DownloadUtils.fileSizeFromInternet(value)
            val anotherSize = oldPost.find { it.feedId == uploadPost.feedId }?.resources?.find { it.url == value }?.size
            val actualFileSize = if (anotherSize != null && anotherSize > fileSize) anotherSize else fileSize
            myPost.resources.add(OfflineResource(value, actualFileSize, 0L, DownloadStatus.NONE))
        }
    }


    return myPost
}
