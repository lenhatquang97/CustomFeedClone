package com.quangln2.customfeed.domain

import com.quangln2.customfeed.data.models.UploadPost
import com.quangln2.customfeed.data.repository.FeedRepository
import retrofit2.Call

class GetAllFeedsUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(): Call<MutableList<UploadPost>> = feedRepository.getAllFeeds()
}