package com.quangln2.customfeedui.domain.usecase

import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.others.callback.GetDataCallback
import kotlinx.coroutines.CoroutineScope

class GetAllFeedsModifiedUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(
        onTakeData: GetDataCallback,
        coroutineScope: CoroutineScope,
        preloadCache: Boolean) = feedRepository.getAllFeedsWithModified(onTakeData, coroutineScope, preloadCache)
}