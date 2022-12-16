package com.quangln2.customfeedui.data.datasource.local

import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.models.others.FeedWrapper

interface LocalDataSource {
    fun compareDBPostsAndFetchPosts(dbPosts: List<MyPost>, fetchedPost: List<MyPost>): Boolean
    suspend fun insert(myPost: MyPost)
    suspend fun getAllFeeds(): List<MyPost>
    suspend fun update(myPost: MyPost)
    suspend fun delete(id: String)
    suspend fun getFeedWithId(feed_id: String): MyPost
    suspend fun handleModifyPostList(body: MutableList<UploadPost>): List<MyPost>
    suspend fun updatePostsBasedOnServer(body: MutableList<UploadPost>): FeedWrapper



}