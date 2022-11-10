package com.quangln2.customfeedui.domain.usecase

import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.repository.FeedRepository

class GetAllInDatabaseUseCase(private val feedRepository: FeedRepository) {
    suspend operator fun invoke(): List<MyPost> = feedRepository.getAll()
}