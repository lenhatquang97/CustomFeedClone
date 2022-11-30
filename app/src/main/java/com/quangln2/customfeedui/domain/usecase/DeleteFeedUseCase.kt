package com.quangln2.customfeedui.domain.usecase

import android.content.Context
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.repository.FeedRepository

class DeleteFeedUseCase(private val feedRepository: FeedRepository) {
     operator fun invoke(id: String, oldLists: List<MyPost>, context: Context) = feedRepository.deleteFeed(id, oldLists, context)
}