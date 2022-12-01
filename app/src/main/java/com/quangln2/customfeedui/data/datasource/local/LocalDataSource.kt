package com.quangln2.customfeedui.data.datasource.local

import android.content.Context
import android.net.Uri
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.models.others.FeedWrapper
import okhttp3.MultipartBody

interface LocalDataSource {
    fun uploadMultipartBuilder(caption: String, uriLists: MutableList<Uri>, context: Context): List<MultipartBody.Part>
    fun compareDBPostsAndFetchPosts(dbPosts: List<MyPost>, fetchedPost: List<MyPost>): Boolean

    suspend fun insert(myPost: MyPost)
    suspend fun getAllFeeds(): List<MyPost>
    suspend fun update(myPost: MyPost)
    suspend fun delete(id: String)
    suspend fun getFeedWithId(feed_id: String): MyPost
    suspend fun handleModifyPostList(body: MutableList<UploadPost>): List<MyPost>
    suspend fun updatePostsBasedOnServer(body: MutableList<UploadPost>): FeedWrapper



}