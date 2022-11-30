package com.quangln2.customfeedui.data.datasource.remote

import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class RemoteDataSourceImpl : RemoteDataSource {
    override fun getAllFeeds(): MutableList<UploadPost> {
        val obj = URL(ConstantSetup.GET_FEEDS)
        val conn = obj.openConnection() as HttpURLConnection
        val uploadList = mutableListOf<UploadPost>()
        conn.requestMethod = "GET"
        conn.connect()

        val responseCode = conn.responseCode
        if(responseCode == HttpURLConnection.HTTP_OK){
            val br = BufferedReader(InputStreamReader(conn.inputStream))
            val result = br.readLine()
            val uploadPost = UploadPost.jsonStringToUploadPostList(result)
            uploadList.addAll(uploadPost)
            br.close()
        }

        conn.disconnect()
        return uploadList
    }

    override fun deleteFeed(id: String): Int {
        val obj = URL(ConstantSetup.DELETE_FEED + id)
        val conn = obj.openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        conn.connect()
        val responseCode = conn.responseCode
        conn.disconnect()
        return responseCode
    }

    override fun uploadPostV2(requestBody: UploadPost): Int {
        val obj = URL(ConstantSetup.UPLOAD_FEED_VERSION_2)
        val conn = obj.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; utf-8")
        conn.setRequestProperty("Accept", "application/json")
        conn.doOutput = true
        val jsonInputString = UploadPost.uploadPostToJsonObject(requestBody).toString()
        val os = conn.outputStream
        val input = jsonInputString.toByteArray(charset("utf-8"))
        os.write(input, 0, input.size)
        conn.connect()
        val responseCode = conn.responseCode
        conn.disconnect()
        return responseCode
    }
}