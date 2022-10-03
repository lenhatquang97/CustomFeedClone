package com.quangln2.customfeed.domain

import com.quangln2.customfeed.repository.FeedRepository

class DeleteFeedUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(id: String) = feedRepository.deleteFeed(id)
}