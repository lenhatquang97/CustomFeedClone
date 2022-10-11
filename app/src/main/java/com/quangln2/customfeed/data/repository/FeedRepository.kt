package com.quangln2.customfeed.data.repository

import android.content.Context
import android.net.Uri
import com.quangln2.customfeed.data.datasource.local.LocalDataSource
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSource
import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.data.models.datamodel.UploadPost
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call

class FeedRepository(private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource) {
    fun uploadPost(requestBody: List<MultipartBody.Part>): Call<ResponseBody> = remoteDataSource.uploadPost(requestBody)
    fun getAllFeeds(): Call<MutableList<UploadPost>> = remoteDataSource.getAllFeeds()
    fun deleteFeed(id: String): Call<ResponseBody> = remoteDataSource.deleteFeed(id)
    fun uploadMultipartBuilder(
        caption: String,
        uriLists: MutableList<Uri>,
        context: Context
    ): List<MultipartBody.Part> = localDataSource.uploadMultipartBuilder(caption, uriLists, context)

    suspend fun insert(myPost: MyPost) = localDataSource.insert(myPost)
    suspend fun update(myPost: MyPost) = localDataSource.update(myPost)
    suspend fun delete(id: String) = localDataSource.delete(id)
    fun getAll(): Flow<List<MyPost>> = localDataSource.getAll()
}