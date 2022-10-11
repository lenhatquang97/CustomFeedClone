package com.quangln2.customfeed.data.models.uimodel

import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.data.models.datamodel.OfflineResource
import java.util.*

data class MyPostRender(
    //Key
    var feedId: String,

    var typeOfPost: String,
    var name: String,
    var avatar: String,
    var createdTime: String,
    var caption: String,
    var resources: MutableList<OfflineResource>
) {
    constructor() : this("", UUID.randomUUID().toString(), "", "", "", "", mutableListOf())

    companion object {
        fun convertMyPostToMyPostRender(myPost: MyPost, typeOfPost: String = UUID.randomUUID().toString()): MyPostRender {
            return MyPostRender(
                feedId = myPost.feedId,
                typeOfPost = typeOfPost,
                name = myPost.name,
                avatar = myPost.avatar,
                createdTime = myPost.createdTime,
                caption = myPost.caption,
                resources = myPost.resources
            )
        }
    }

}