package com.quangln2.customfeedui.data.repository

import com.quangln2.customfeedui.data.datasource.local.LocalDataSource
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSource
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import okhttp3.ResponseBody
import retrofit2.Call

class FeedRepository(private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource) {
    fun getAllFeeds(): Call<MutableList<UploadPost>> = remoteDataSource.getAllFeeds()
    fun deleteFeed(id: String): Call<ResponseBody> = remoteDataSource.deleteFeed(id)


    suspend fun insert(myPost: MyPost) = localDataSource.insert(myPost)
    suspend fun delete(id: String) = localDataSource.delete(id)
    suspend fun getAll(): List<MyPost> = localDataSource.getAll()
    fun uploadPostV2(requestBody: UploadPost): Call<ResponseBody> = remoteDataSource.uploadPostV2(requestBody)

}