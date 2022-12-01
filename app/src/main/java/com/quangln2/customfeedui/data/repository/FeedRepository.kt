package com.quangln2.customfeedui.data.repository

import android.content.Context
import android.widget.Toast
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.datasource.local.LocalDataSource
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSource
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.models.others.EnumFeedLoadingCode
import com.quangln2.customfeedui.data.models.others.FeedWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class FeedRepository(private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource) {
    fun getAllFeedsWithModified(preloadCache: Boolean): Flow<FeedWrapper> = flow{
         //Step 1: Preload cache if necessary
         if(preloadCache){
             val offlinePosts = localDataSource.getAllFeeds()
             emit(FeedWrapper(offlinePosts, EnumFeedLoadingCode.OFFLINE.value))
         }
         //Step 2: Fetch data from server
         val body = remoteDataSource.getAllFeeds()
         val result = localDataSource.updatePostsBasedOnServer(body)
         emit(result)
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


}