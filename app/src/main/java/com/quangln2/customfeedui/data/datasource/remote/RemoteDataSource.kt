package com.quangln2.customfeedui.data.datasource.remote

import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import java.io.File


interface RemoteDataSource {
    fun uploadPostV2(requestBody: UploadPost): Int
    fun getAllFeeds(): MutableList<UploadPost>
    fun deleteFeed(id: String): Int
    fun uploadFileWithId(url: String, file: File, id: String, checksum: String): String
}