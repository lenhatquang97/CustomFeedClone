package com.quangln2.customfeed.data.database

import com.quangln2.customfeed.data.models.DownloadStatus
import com.quangln2.customfeed.data.models.MyPost
import com.quangln2.customfeed.data.models.OfflineResource
import com.quangln2.customfeed.data.models.UploadPost
import com.quangln2.customfeed.others.utils.DownloadUtils

fun convertFromUploadPostToMyPost(uploadPost: UploadPost, oldPost: List<MyPost>): MyPost {
    val myPost = MyPost()
    myPost.feedId = uploadPost.feedId
    myPost.avatar = uploadPost.avatar
    myPost.name = uploadPost.name
    myPost.caption = uploadPost.caption
    myPost.createdTime = uploadPost.createdTime
    for(i in 0 until uploadPost.imagesAndVideos.size){
        val value = uploadPost.imagesAndVideos[i]
        val fileSize = DownloadUtils.fileSizeFromInternet(value)
        val anotherSize = oldPost.find { it.feedId == uploadPost.feedId }?.resources?.find { it.url == value }?.size
        val actualFileSize = if (anotherSize != null && anotherSize > fileSize) anotherSize else fileSize
        myPost.resources.add(OfflineResource(value, "", actualFileSize, 0L, DownloadStatus.NONE))
    }

    return myPost
}

fun convertFromMyPostToUploadPost(myPost: MyPost) : UploadPost{
    val uploadPost = UploadPost()
    uploadPost.feedId = myPost.feedId
    uploadPost.avatar = myPost.avatar
    uploadPost.name = myPost.name
    uploadPost.caption = myPost.caption
    uploadPost.createdTime = myPost.createdTime
    for(value in myPost.resources){
        uploadPost.imagesAndVideos.add(value.url)
    }
    return uploadPost
}