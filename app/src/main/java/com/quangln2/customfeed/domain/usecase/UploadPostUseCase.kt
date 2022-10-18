package com.quangln2.customfeed.domain.usecase

import com.quangln2.customfeed.data.repository.FeedRepository
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call

class UploadPostUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(requestBody: List<MultipartBody.Part>): Call<ResponseBody> =
        feedRepository.uploadPost(requestBody)

}