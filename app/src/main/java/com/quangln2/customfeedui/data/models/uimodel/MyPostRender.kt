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
        fun convertMyPostToMyPostRender(myPost: MyPost): MyPostRender {
            return MyPostRender(
                feedId = myPost.feedId,
                typeOfPost = TypeOfPost.POST,
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
            val addNewPostItem = convertMyPostToMyPostRender(MyPost()).copy(typeOfPost = TypeOfPost.ADD_NEW_POST)
            listsOfPostRender.add(addNewPostItem)
            for(i in posts.indices){
                val myPostRender = convertMyPostToMyPostRender(posts[i])
                for(j in 2..4){
                    listsOfPostRender.add(myPostRender.copy(typeOfPost = getTypeOfPost(j)))
                }
            }
            return listsOfPostRender
        }
    }

}