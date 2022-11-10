package com.quangln2.customfeedui.domain.usecase

import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.repository.FeedRepository

class InsertDatabaseUseCase(private val feedRepository: FeedRepository) {
    suspend operator fun invoke(myPost: MyPost) = feedRepository.insert(myPost)
}