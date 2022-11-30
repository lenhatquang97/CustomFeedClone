package com.quangln2.customfeedui.domain.usecase

import com.quangln2.customfeedui.data.repository.FeedRepository

class GetAllFeedsModifiedUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(preloadCache: Boolean) = feedRepository.getAllFeedsWithModified(preloadCache)
}