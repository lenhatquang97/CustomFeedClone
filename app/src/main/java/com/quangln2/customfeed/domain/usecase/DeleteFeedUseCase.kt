package com.quangln2.customfeed.domain.usecase

import com.quangln2.customfeed.data.repository.FeedRepository

class DeleteFeedUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(id: String) = feedRepository.deleteFeed(id)
}