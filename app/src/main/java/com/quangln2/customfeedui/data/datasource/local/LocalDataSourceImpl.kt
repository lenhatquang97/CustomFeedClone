package com.quangln2.customfeedui.data.datasource.local

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.database.FeedDao
import com.quangln2.customfeedui.data.database.convertFromUploadPostToMyPost
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.models.others.EnumFeedLoadingCode
import com.quangln2.customfeedui.data.models.others.FeedWrapper
import com.quangln2.customfeedui.others.utils.DownloadUtils.getMimeType
import com.quangln2.customfeedui.others.utils.FileUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
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


    override fun uploadMultipartBuilder(
        caption: String,
        uriLists: MutableList<Uri>,
        context: Context
    ): List<MultipartBody.Part> {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("feedId", UUID.randomUUID().toString())
        builder.addFormDataPart("name", context.resources.getString(R.string.account_name))
        builder.addFormDataPart("avatar", ConstantSetup.AVATAR_LINK)
        builder.addFormDataPart("createdTime", System.currentTimeMillis().toString())
        builder.addFormDataPart("caption", caption)

        for (uriItr in uriLists) {
            val tmp = FileUtils.getRealPathFromURI(uriItr, context)
            if (tmp != null) {
                val file = File(tmp)

                val requestFile = file.asRequestBody(getMimeType(file.toURI().toString())?.toMediaTypeOrNull())
                builder.addFormDataPart("upload", file.name, requestFile)
            }
        }
        return builder.build().parts
    }

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
            val availableItems = handleModifyPostList(body)
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