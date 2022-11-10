package com.quangln2.customfeedui.domain.usecase

import com.quangln2.customfeedui.data.repository.FeedRepository

class DeleteDatabaseUseCase(private val feedRepository: FeedRepository) {
    suspend operator fun invoke(id: String) = feedRepository.delete(id)
}