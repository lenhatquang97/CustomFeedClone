package com.quangln2.customfeed.domain

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.quangln2.customfeed.data.repository.FeedRepository
import okhttp3.MultipartBody

class UploadMultipartBuilderUseCase(private val feedRepository: FeedRepository) {
    operator fun invoke(
        caption: String,
        uriLists: LiveData<MutableList<Uri>>,
        context: Context
    ): List<MultipartBody.Part> {
        return feedRepository.uploadMultipartBuilder(caption, uriLists, context)
    }
}