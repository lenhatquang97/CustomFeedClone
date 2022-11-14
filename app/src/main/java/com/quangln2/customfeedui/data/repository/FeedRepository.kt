package com.quangln2.customfeedui.data.repository

import android.content.Context
import android.net.Uri
import com.quangln2.customfeedui.data.datasource.local.LocalDataSource
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSource
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
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
    suspend fun getAll(): List<MyPost> = localDataSource.getAll()
    suspend fun getListById(id: String): MyPost = localDataSource.getFeedWithId(id)
    fun uploadPostV2(requestBody: UploadPost): Call<ResponseBody> = remoteDataSource.uploadPostV2(requestBody)

}