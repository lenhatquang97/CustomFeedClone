package com.quangln2.customfeedui.data.datasource.remote

import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call


interface RemoteDataSource {
    fun uploadPost(requestBody: List<MultipartBody.Part>): Call<ResponseBody>
    fun getAllFeeds(): Call<MutableList<UploadPost>>
    fun deleteFeed(id: String): Call<ResponseBody>
}