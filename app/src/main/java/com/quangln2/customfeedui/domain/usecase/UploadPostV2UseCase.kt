package com.quangln2.customfeedui.domain.usecase

import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.repository.FeedRepository
import okhttp3.ResponseBody
import retrofit2.Call

class UploadPostV2UseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(requestBody: UploadPost): Call<ResponseBody> =
        feedRepository.uploadPostV2(requestBody)

}