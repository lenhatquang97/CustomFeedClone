package com.quangln2.customfeedui.data.datasource.local

import androidx.annotation.WorkerThread
import com.quangln2.customfeedui.data.database.FeedDao
import com.quangln2.customfeedui.data.database.convertFromUploadPostToMyPost
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.models.others.EnumFeedLoadingCode
import com.quangln2.customfeedui.data.models.others.FeedWrapper
import java.util.*

class LocalDataSourceImpl(private val feedDao: FeedDao) : LocalDataSource {
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun getFeedWithId(feed_id: String): MyPost = feedDao.getFeedWithId(feed_id)
    override suspend fun handleModifyPostList(body: MutableList<UploadPost>): List<MyPost> {
        val ls = mutableListOf<MyPost>()
        val deletedFeeds = mutableListOf<MyPost>()
        val offlinePosts = getAllFeeds()
        body.forEach {
            val itemConverted = convertFromUploadPostToMyPost(it, offlinePosts)
            ls.add(itemConverted)
        }

        //find in offline feeds if there are no online posts in online database
        offlinePosts.forEach {
            val filterId = body.find { item -> item.feedId == it.feedId }
            if (filterId == null) {
                deletedFeeds.add(it)
            }
        }

        //Deleted first
        deletedFeeds.forEach {
            delete(it.feedId)
        }

        val availableItems = ls.filter { item -> deletedFeeds.find { it.feedId == item.feedId } == null }
        availableItems.forEach {
            insert(it)
        }
        return availableItems
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(myPost: MyPost) {
        if (feedDao.existsWithId(myPost.feedId) == 0) {
            feedDao.insert(myPost)
        } else {
            feedDao.update(myPost)
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun getAllFeeds(): List<MyPost> = feedDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun update(myPost: MyPost) = feedDao.update(myPost)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun delete(id: String) = feedDao.delete(id)

    override fun compareDBPostsAndFetchPosts(dbPosts: List<MyPost>, fetchedPost: List<MyPost>): Boolean{
        if(dbPosts.size != fetchedPost.size) return false
        for((a,b) in dbPosts.zip(fetchedPost)){
            if(a != b) return false
        }
        return true
    }

    override suspend fun updatePostsBasedOnServer(body: MutableList<UploadPost>): FeedWrapper {
        return if(body.isNotEmpty()){
            //Step 3: Compare data from server and data from local
            val offlinePosts = getAllFeeds()
            val availableItems = handleModifyPostList(body).sortedByDescending { it.createdTime.toBigInteger() }
            if(!compareDBPostsAndFetchPosts(offlinePosts, availableItems)){
                FeedWrapper(availableItems, EnumFeedLoadingCode.SUCCESS.value)
            } else {
                FeedWrapper(offlinePosts, EnumFeedLoadingCode.SUCCESS.value)
            }
        } else {
            //Step 4: If there is no data from server, return data from local
            val offlinePosts = getAllFeeds()
            FeedWrapper(offlinePosts, EnumFeedLoadingCode.OFFLINE.value)
        }
    }
}