package com.quangln2.customfeed.domain

import com.quangln2.customfeed.data.models.MyPost
import com.quangln2.customfeed.data.repository.FeedRepository

class DeleteDatabaseUseCase(private val feedRepository: FeedRepository) {
    suspend operator fun invoke(myPost: MyPost) = feedRepository.delete(myPost)
}