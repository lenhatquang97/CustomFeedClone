package com.quangln2.customfeedui.imageloader.data.network

import android.content.Context
import android.net.Uri
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpFetcher {
    companion object{
        fun fetchImageByInputStream(uri: Uri, context: Context): InputStream?{
            val inputStream = context.contentResolver.openInputStream(uri)
            val byteArray = inputStream?.readBytes()
            inputStream?.close()
            return byteArray?.inputStream()
        }

        fun downloadImageUsingInputStream(inputStream: InputStream, fileName: String){
            val fileOutputStream = FileOutputStream(fileName)
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                fileOutputStream.write(buffer, 0, len)
            }
            fileOutputStream.close()
            inputStream.close()
        }


        fun fetchImageByInputStream(url: String) : InputStream?{
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            try{
                conn.connect()
                val responseCode = conn.responseCode
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    //fetch input stream
                    val inputStream = conn.inputStream
                    val byteArray = inputStream.readBytes()
                    conn.disconnect()
                    return byteArray.inputStream()
                }
                conn.disconnect()
            } catch(e: java.lang.Exception){
                e.printStackTrace()
            }
            return null
        }
    }
}