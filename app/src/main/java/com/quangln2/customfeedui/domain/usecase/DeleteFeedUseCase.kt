package com.quangln2.customfeedui.domain.usecase

import com.quangln2.customfeedui.data.repository.FeedRepository

class DeleteFeedUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(id: String) = feedRepository.deleteFeed(id)
}