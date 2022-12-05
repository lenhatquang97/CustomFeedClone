package com.quangln2.customfeedui.domain.usecase

import com.quangln2.customfeedui.data.repository.FeedRepository
import java.io.File

class UploadFileWithPostIdUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(url: String, file: File, postId: String): String =
        feedRepository.uploadFileWithPostId(url, file, postId)

}