package com.quangln2.customfeed.domain.usecase

import com.quangln2.customfeed.data.repository.FeedRepository

class DeleteDatabaseUseCase(private val feedRepository: FeedRepository) {
    suspend operator fun invoke(id: String) = feedRepository.delete(id)
}