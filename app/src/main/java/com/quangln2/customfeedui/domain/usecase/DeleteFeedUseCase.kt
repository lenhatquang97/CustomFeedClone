package com.quangln2.customfeedui.domain.usecase

import android.content.Context
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.others.callback.GetDataCallback
import kotlinx.coroutines.CoroutineScope

class DeleteFeedUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(id: String, onTakeData: GetDataCallback, oldLists: List<MyPost>, coroutineScope: CoroutineScope, context: Context) = feedRepository.deleteFeed(id, onTakeData, oldLists, coroutineScope, context)
}