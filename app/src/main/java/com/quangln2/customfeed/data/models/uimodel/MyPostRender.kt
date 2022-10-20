package com.quangln2.customfeed.data.models.uimodel

import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.data.models.datamodel.OfflineResource

data class MyPostRender(
    //Key
    var feedId: String,

    var typeOfPost: TypeOfPost,
    var name: String,
    var avatar: String,
    var createdTime: String,
    var caption: String,
    var resources: MutableList<OfflineResource>,
    var firstItemWidth: Int,
    var firstItemHeight: Int,
) {
    constructor() : this("", TypeOfPost.POST, "", "", "", "", mutableListOf(),0,0)

    companion object {
        fun convertMyPostToMyPostRender(myPost: MyPost, typeOfPost: TypeOfPost = TypeOfPost.POST): MyPostRender {
            return MyPostRender(
                feedId = myPost.feedId,
                typeOfPost = typeOfPost,
                name = myPost.name,
                avatar = myPost.avatar,
                createdTime = myPost.createdTime,
                caption = myPost.caption,
                resources = myPost.resources,
                firstItemWidth = 0,
                firstItemHeight = 0,
            )
        }
    }

}