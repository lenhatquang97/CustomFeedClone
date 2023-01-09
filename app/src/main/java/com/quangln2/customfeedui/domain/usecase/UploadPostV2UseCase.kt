package com.quangln2.customfeedui.domain.usecase

import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.repository.FeedRepository

class UploadPostV2UseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(requestBody: UploadPost): Int =
        feedRepository.uploadPostV2(requestBody)
}