package com.quangln2.customfeedui.data.datasource.remote

import com.quangln2.customfeedui.data.models.datamodel.UploadPost


interface RemoteDataSource {
    fun uploadPostV2(requestBody: UploadPost): Int
    fun getAllFeeds(): MutableList<UploadPost>
    fun deleteFeed(id: String): Int
}