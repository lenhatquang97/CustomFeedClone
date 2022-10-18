package com.quangln2.customfeed.domain.usecase

import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.data.repository.FeedRepository

class GetAllInDatabaseUseCase(private val feedRepository: FeedRepository) {
    suspend operator fun invoke(): List<MyPost> = feedRepository.getAll()
}