package com.quangln2.customfeed.domain

import com.quangln2.customfeed.data.models.MyPost
import com.quangln2.customfeed.data.repository.FeedRepository
import kotlinx.coroutines.flow.Flow

class GetAllInDatabaseUseCase(private val feedRepository: FeedRepository) {
   operator fun invoke(): Flow<List<MyPost>> = feedRepository.getAll()
}