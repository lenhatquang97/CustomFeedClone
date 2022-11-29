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
import com.quangln2.customfeedui.others.callback.GetDataCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedRepository(private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource) {
     fun getAllFeedsWithModified(onTakeData: GetDataCallback, coroutineScope: CoroutineScope, preloadCache: Boolean){
         coroutineScope.launch(Dispatchers.IO) {
             if(preloadCache){
                 loadCache(coroutineScope, onTakeData)
             }
             val response = remoteDataSource.getAllFeeds().execute()
             if(response.isSuccessful){
                 if (response.code() == 200) {
                     val ls = mutableListOf<MyPost>()
                     val body = response.body()
                     val deletedFeeds = mutableListOf<MyPost>()
                     val offlinePosts = localDataSource.getAll()
                     if(body != null){
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
                     }

                     //get available items but not in deleted feeds
                     val availableItems = ls.filter { item -> deletedFeeds.find { it.feedId == item.feedId } == null }
                     if(!compareDBPostsAndFetchPosts(offlinePosts, availableItems))
                         onTakeData.onGetUploadList(availableItems)
                 } else{
                     onTakeData.onGetFeedLoadingCode(EnumFeedLoadingCode.OFFLINE.value)
                     val offlinePosts = localDataSource.getAll()
                     if(offlinePosts.isNotEmpty()) {
                         onTakeData.onGetUploadList(offlinePosts.toMutableList())
                     }
                 }
                 onTakeData.onGetFeedLoadingCode(response.code())
             } else {
                 loadCache(coroutineScope, onTakeData)
             }
         }

    }

    fun deleteFeed(id: String, onTakeData: GetDataCallback, oldLists: List<MyPost>, coroutineScope: CoroutineScope, context: Context) {
        remoteDataSource.deleteFeed(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.errorBody() == null && response.code() == 200) {
                    coroutineScope.launch(Dispatchers.IO) {
                        localDataSource.delete(id)
                    }
                    val filteredList = oldLists.filter { it.feedId != id }
                    onTakeData.onGetUploadList(filteredList)
                    Toast.makeText(context, context.resources.getString(R.string.delete_successfully), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.resources.getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, context.resources.getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
            }

        })
    }

    suspend fun delete(id: String) = localDataSource.delete(id)
    fun uploadPostV2(requestBody: UploadPost): Call<ResponseBody> = remoteDataSource.uploadPostV2(requestBody)



    private fun loadCache(coroutineScope: CoroutineScope, onTakeData: GetDataCallback) {
        onTakeData.onGetFeedLoadingCode(EnumFeedLoadingCode.OFFLINE.value)
        coroutineScope.launch(Dispatchers.IO) {
            val offlinePosts = localDataSource.getAll()
            if(offlinePosts.isNotEmpty()) {
                onTakeData.onGetUploadList(offlinePosts.toMutableList())
            }
        }

    }

    private fun compareDBPostsAndFetchPosts(dbPosts: List<MyPost>, fetchedPost: List<MyPost>): Boolean{
        if(dbPosts.size != fetchedPost.size) return false
        for((a,b) in dbPosts.zip(fetchedPost)){
            if(a != b) return false
        }
        return true

    }

}