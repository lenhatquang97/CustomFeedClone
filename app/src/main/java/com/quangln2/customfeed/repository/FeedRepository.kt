package com.quangln2.customfeed.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.quangln2.customfeed.datasource.local.LocalDataSource
import com.quangln2.customfeed.datasource.remote.RemoteDataSource
import com.quangln2.customfeed.models.UploadPost
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call

class FeedRepository(private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource) {
    fun uploadPost(requestBody: List<MultipartBody.Part>): Call<ResponseBody> = remoteDataSource.uploadPost(requestBody)
    fun getAllFeeds(): Call<MutableList<UploadPost>> = remoteDataSource.getAllFeeds()
    fun deleteFeed(id: String): Call<ResponseBody> = remoteDataSource.deleteFeed(id)
    fun uploadMultipartBuilder(
        caption: String,
        uriLists: LiveData<MutableList<Uri>>,
        context: Context
    ): List<MultipartBody.Part> = localDataSource.uploadMultipartBuilder(caption, uriLists, context)
}