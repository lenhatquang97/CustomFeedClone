package com.quangln2.customfeed.domain

import android.content.Context
import android.net.Uri
import com.quangln2.customfeed.data.repository.FeedRepository
import okhttp3.MultipartBody

class UploadMultipartBuilderUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(
        caption: String,
        uriLists: MutableList<Uri>,
        context: Context
    ): List<MultipartBody.Part> {
        return feedRepository.uploadMultipartBuilder(caption, uriLists, context)
    }
}