package com.quangln2.customfeed.data.database

import com.quangln2.customfeed.data.models.DownloadStatus
import com.quangln2.customfeed.data.models.MyPost
import com.quangln2.customfeed.data.models.OfflineResource
import com.quangln2.customfeed.data.models.UploadPost

fun convertFromUploadPostToMyPost(uploadPost: UploadPost): MyPost {
    val myPost = MyPost()
    myPost.feedId = uploadPost.feedId
    myPost.avatar = uploadPost.avatar
    myPost.name = uploadPost.name
    myPost.caption = uploadPost.caption
    myPost.createdTime = uploadPost.createdTime
    for(value in uploadPost.imagesAndVideos){
        myPost.resources.add(OfflineResource(value, "", 0L, 0L, DownloadStatus.NONE))
    }

    return myPost
}