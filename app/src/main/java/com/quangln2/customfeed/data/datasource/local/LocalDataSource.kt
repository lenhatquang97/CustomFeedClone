package com.quangln2.customfeed.data.datasource.local

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import okhttp3.MultipartBody

interface LocalDataSource {
    fun uploadMultipartBuilder(
        caption: String,
        uriLists: LiveData<MutableList<Uri>>,
        context: Context
    ): List<MultipartBody.Part>
}