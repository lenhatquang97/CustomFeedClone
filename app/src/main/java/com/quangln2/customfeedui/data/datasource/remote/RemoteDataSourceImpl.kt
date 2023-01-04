package com.quangln2.customfeedui.data.datasource.remote

import android.util.Log
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class RemoteDataSourceImpl : RemoteDataSource {
    private fun readFileToByteArray(file: File): ByteArray{
        //read byte to byte
        val fileInputStream = file.inputStream()
        val byteArray = ByteArray(file.length().toInt())
        var read = 0
        var offset = 0
        while (read != -1 && offset < file.length()){
            read = fileInputStream.read(byteArray, offset, (file.length() - offset).toInt())
            offset += read
        }
        return byteArray
    }

    override fun getAllFeeds(): MutableList<UploadPost> {
        val obj = URL(ConstantSetup.GET_FEEDS)
        val conn = obj.openConnection() as HttpURLConnection
        val uploadList = mutableListOf<UploadPost>()
        conn.requestMethod = "GET"
        try{
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
        } catch (e: Exception){
            e.printStackTrace()
        }
        return uploadList
    }

    override fun deleteFeed(id: String): Int {
        val obj = URL(ConstantSetup.DELETE_FEED + id)
        val conn = obj.openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        var responseCode = 400
        try{
            conn.connect()
            responseCode = conn.responseCode
            conn.disconnect()
        } catch (e: Exception){
            e.printStackTrace()
        }
        return responseCode
    }

    override fun uploadPostV2(requestBody: UploadPost): Int {
        var responseCode = 400
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
        try{
            conn.connect()
            responseCode = conn.responseCode
            conn.disconnect()
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            os.close()
        }
        return responseCode
    }

    override fun uploadFileWithId(url: String, file: File, id: String): String{
        val connection = URL(url).openConnection() as HttpURLConnection
        val boundary = UUID.randomUUID().toString()
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
        connection.setRequestProperty("Connection", "Keep-Alive")
        try{
            connection.connect()
            val request = DataOutputStream(connection.outputStream)
            request.writeBytes("--$boundary\r\n")
            request.writeBytes("Content-Disposition: form-data; name=\"id\"\r\n\r\n")
            request.writeBytes(id + "\r\n")
            request.writeBytes("--$boundary\r\n")
            request.writeBytes("Content-Disposition: form-data; name=\"description\"\r\n\r\n")
            request.writeBytes(file.name + "\r\n")
            request.writeBytes("--$boundary\r\n")
            request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"\r\n\r\n")
            request.write(readFileToByteArray(file))
            request.writeBytes("\r\n")
            request.writeBytes("--$boundary--\r\n")
            request.flush()
            val responseCode = connection.responseCode
            if(responseCode == HttpURLConnection.HTTP_OK) {
                val br = BufferedReader(InputStreamReader(connection.inputStream))
                val result = br.readLine()
                br.close()
                connection.disconnect()
                return result
            } else if(responseCode == 307){
                connection.disconnect()
                return uploadFileWithId(connection.url.toString(), file, id)
            }
            else {
                Log.d("Failed", "Upload file failed $responseCode")
            }

        } catch (e: java.lang.Exception){
            e.printStackTrace()
        }
        return "error"
    }
}