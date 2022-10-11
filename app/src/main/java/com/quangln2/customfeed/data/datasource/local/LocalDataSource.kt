package com.quangln2.customfeed.data.datasource.local

import android.content.Context
import android.net.Uri
import com.quangln2.customfeed.data.models.datamodel.MyPost
import okhttp3.MultipartBody

interface LocalDataSource {
    fun uploadMultipartBuilder(
        caption: String,
        uriLists: MutableList<Uri>,
        context: Context
    ): List<MultipartBody.Part>

    suspend fun insert(myPost: MyPost)
    suspend fun getAll(): List<MyPost>
    suspend fun update(myPost: MyPost)
    suspend fun delete(id: String)
}