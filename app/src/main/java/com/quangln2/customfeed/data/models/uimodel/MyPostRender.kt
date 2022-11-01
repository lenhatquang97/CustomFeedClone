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
    var firstItemWidth: Int = 0,
    var firstItemHeight: Int = 0,
    var containsVideo: Boolean = false
) {
    constructor() : this("", TypeOfPost.POST, "", "", "", "", mutableListOf(), 0, 0, false)

    companion object {
        fun convertMyPostToMyPostRender(myPost: MyPost, typeOfPost: TypeOfPost = TypeOfPost.POST): MyPostRender {
            var containsVideo = false
            for(resource in myPost.resources) {
                if(resource.url.contains(".mp4.mp4")){
                    containsVideo = true
                    break
                }
            }
            return MyPostRender(
                feedId = myPost.feedId,
                typeOfPost = typeOfPost,
                name = myPost.name,
                avatar = myPost.avatar,
                createdTime = myPost.createdTime,
                caption = myPost.caption,
                resources = myPost.resources,
                firstItemHeight = 0,
                firstItemWidth = 0,
                containsVideo = containsVideo
            )
        }
    }

}