package com.quangln2.customfeed.domain.usecase

import com.quangln2.customfeed.data.models.datamodel.UploadPost
import com.quangln2.customfeed.data.repository.FeedRepository
import retrofit2.Call

class GetAllFeedsUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(): Call<MutableList<UploadPost>> = feedRepository.getAllFeeds()
}