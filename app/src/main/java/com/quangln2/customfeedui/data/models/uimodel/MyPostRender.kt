package com.quangln2.customfeedui.data.models.uimodel

import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.datamodel.OfflineResource

data class MyPostRender(
    //Primary key
    var feedId: String,
    var typeOfPost: TypeOfPost,
    var name: String,
    var avatar: String,
    var createdTime: String,
    var caption: String,
    var resources: MutableList<OfflineResource>,
    var firstItemWidth: Int = 0,
    var firstItemHeight: Int = 0
) {

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
                firstItemHeight = myPost.firstHeight,
                firstItemWidth = myPost.firstWidth
            )
        }

        fun convertToListWithRenderedPost(posts: List<MyPost>): List<MyPostRender>{
            val listsOfPostRender = mutableListOf<MyPostRender>()
            val addNewPostItem = convertMyPostToMyPostRender(MyPost(), TypeOfPost.ADD_NEW_POST)
            listsOfPostRender.add(addNewPostItem)
            posts.forEach { itr ->
                val myPostRender = convertMyPostToMyPostRender(itr)
                listsOfPostRender.add(myPostRender)
            }
            return listsOfPostRender
        }
    }

}