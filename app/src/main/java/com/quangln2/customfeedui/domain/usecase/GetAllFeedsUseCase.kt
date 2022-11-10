package com.quangln2.customfeedui.domain.usecase

import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.repository.FeedRepository
import retrofit2.Call

class GetAllFeedsUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(): Call<MutableList<UploadPost>> = feedRepository.getAllFeeds()
}