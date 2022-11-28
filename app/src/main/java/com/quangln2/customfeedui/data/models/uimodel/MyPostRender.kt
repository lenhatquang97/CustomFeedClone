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
<<<<<<< HEAD:app/src/main/java/com/quangln2/customfeed/data/models/uimodel/MyPostRender.kt
    var firstItemWidth: Int,
    var firstItemHeight: Int,
) {
    constructor() : this("", TypeOfPost.POST, "", "", "", "", mutableListOf(),0,0)
=======
    var firstItemWidth: Int = 0,
    var firstItemHeight: Int = 0
) {
>>>>>>> master:app/src/main/java/com/quangln2/customfeedui/data/models/uimodel/MyPostRender.kt

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
<<<<<<< HEAD:app/src/main/java/com/quangln2/customfeed/data/models/uimodel/MyPostRender.kt
                firstItemWidth = 0,
                firstItemHeight = 0,
=======
                firstItemHeight = myPost.firstHeight,
                firstItemWidth = myPost.firstWidth
>>>>>>> master:app/src/main/java/com/quangln2/customfeedui/data/models/uimodel/MyPostRender.kt
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