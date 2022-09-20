package com.quangln2.customfeed.datasource.remote

import com.quangln2.customfeed.models.UploadPost
import com.quangln2.customfeed.singleton.RetrofitSetup
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call


class RemoteDataSource {
    companion object {
        fun uploadFiles(requestBody: List<MultipartBody.Part>): Call<ResponseBody> {
            return RetrofitSetup.gitHubService.uploadFeeds(requestBody)
        }

        fun getAllFeeds(): Call<MutableList<UploadPost>> {
            return RetrofitSetup.gitHubService.getAllFeeds()
        }

        fun deleteFeed(id: String): Call<ResponseBody> {
            return RetrofitSetup.gitHubService.deleteFeed(id)
        }
    }


}