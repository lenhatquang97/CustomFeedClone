package com.quangln2.customfeed.domain

import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.data.repository.FeedRepository

class InsertDatabaseUseCase(private val feedRepository: FeedRepository) {
    suspend operator fun invoke(myPost: MyPost) = feedRepository.insert(myPost)
}