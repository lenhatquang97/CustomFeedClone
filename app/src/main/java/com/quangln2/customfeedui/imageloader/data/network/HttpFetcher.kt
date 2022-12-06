package com.quangln2.customfeedui.imageloader.data.network

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpFetcher {
    private var webUrl = ""
    private var fileUri: Uri? = null
    constructor(webUrl: String){
        this.webUrl = webUrl
    }
    constructor(fileUri: Uri){
        this.fileUri = fileUri
    }

    private fun writeFromInputStream(inputStream: InputStream, fileName: String, context: Context){
        val cacheFile = File(context.cacheDir, fileName)
        val fileOutputStream = FileOutputStream(cacheFile)
        val buffer = ByteArray(1024)
        var len: Int
        while (inputStream.read(buffer).also { len = it } != -1) {
            fileOutputStream.write(buffer, 0, len)
        }
        fileOutputStream.close()
        inputStream.close()
    }

    fun downloadImage(context: Context){
        val conn = URL(webUrl).openConnection() as HttpURLConnection
        val fileName = URLUtil.guessFileName(webUrl, null, null)
        conn.requestMethod = "GET"
        try{
            conn.connect()
            val responseCode = conn.responseCode
            if(responseCode == HttpURLConnection.HTTP_OK) {
                //fetch input stream
                val inputStream = conn.inputStream
                writeFromInputStream(inputStream, fileName, context)
            }
            conn.disconnect()
        } catch(e: java.lang.Exception){
            e.printStackTrace()
        }
    }


    fun fetchImageByInputStream(context: Context): InputStream?{
        if(fileUri != null){
            val inputStream = context.contentResolver.openInputStream(fileUri!!)
            val byteArray = inputStream?.readBytes()
            inputStream?.close()
            return byteArray?.inputStream()
        }
        return null
    }
}