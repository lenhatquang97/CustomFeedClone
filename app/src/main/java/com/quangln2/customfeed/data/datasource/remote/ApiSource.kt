package com.quangln2.customfeed.data.datasource.remote

import com.quangln2.customfeed.data.models.UploadPost
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiSource {
    @GET("feeds")
    fun getAllFeeds(): Call<MutableList<UploadPost>>

    @Multipart
    @POST("feeds/upload")
    fun uploadFeeds(@Part requestBody: List<MultipartBody.Part>): Call<ResponseBody>

    @DELETE("feeds/{id}")
    fun deleteFeed(@Path("id") id: String): Call<ResponseBody>

}
