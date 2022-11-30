package com.quangln2.customfeedui.data.repository

import android.content.Context
import android.widget.Toast
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.database.convertFromUploadPostToMyPost
import com.quangln2.customfeedui.data.datasource.local.LocalDataSource
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSource
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.models.others.EnumFeedLoadingCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class FeedRepository(private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource) {
    fun getAllFeedsWithModified(preloadCache: Boolean): Flow<String> = flow{
         if(preloadCache){
             emit("onGetFeedLoadingCode ${EnumFeedLoadingCode.OFFLINE.value}")
             val offlinePosts = localDataSource.getAll()
             if(offlinePosts.isNotEmpty()) {
                 emit(MyPost.listToJsonString(offlinePosts))
             }
         }
         val body = remoteDataSource.getAllFeeds()
         if(body.isNotEmpty()){
             val ls = mutableListOf<MyPost>()
             val deletedFeeds = mutableListOf<MyPost>()
             val offlinePosts = localDataSource.getAll()
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
                 localDataSource.delete(it.feedId)
             }

             val availableItems = ls.filter { item -> deletedFeeds.find { it.feedId == item.feedId } == null }
             availableItems.forEach {
                 localDataSource.insert(it)
             }
             if(!compareDBPostsAndFetchPosts(offlinePosts, availableItems))
                 emit(MyPost.listToJsonString(availableItems))
             emit("onGetFeedLoadingCode 200")
         } else {
             emit("onGetFeedLoadingCode ${EnumFeedLoadingCode.OFFLINE.value}")
             val offlinePosts = localDataSource.getAll()
             if(offlinePosts.isNotEmpty()) {
                 emit(MyPost.listToJsonString(offlinePosts))
             }
         }

    }.flowOn(Dispatchers.IO)

    fun deleteFeed(id: String, oldLists: List<MyPost>, context: Context): Flow<String> = flow {
        val response = remoteDataSource.deleteFeed(id)
        if(response == 200){
            localDataSource.delete(id)
            val filteredList = oldLists.filter { it.feedId != id }
            emit(MyPost.listToJsonString(filteredList))
            withContext(Dispatchers.Main){
                Toast.makeText(context, context.resources.getString(R.string.delete_successfully), Toast.LENGTH_SHORT).show()
            }
        } else {
            withContext(Dispatchers.Main){
                Toast.makeText(context, context.resources.getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
            }

        }
    }.flowOn(Dispatchers.IO)

    suspend fun delete(id: String) = localDataSource.delete(id)
    suspend fun retrieveItemWithId(id: String) = localDataSource.getFeedWithId(id)
    fun uploadPostV2(requestBody: UploadPost) = remoteDataSource.uploadPostV2(requestBody)

    private fun compareDBPostsAndFetchPosts(dbPosts: List<MyPost>, fetchedPost: List<MyPost>): Boolean{
        if(dbPosts.size != fetchedPost.size) return false
        for((a,b) in dbPosts.zip(fetchedPost)){
            if(a != b) return false
        }
        return true
    }


}