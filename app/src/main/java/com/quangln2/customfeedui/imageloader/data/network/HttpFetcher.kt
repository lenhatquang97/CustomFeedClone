package com.quangln2.customfeedui.imageloader.data.network

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.net.toUri
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.threadpool.TaskExecutor
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpFetcher {
    private var webUrl = ""
    private var fileUri:  Uri? = null
    constructor(webUrl: String){
        this.webUrl = webUrl
    }
    constructor(fileUri: Uri){
        this.fileUri = fileUri
    }

    fun downloadImage(context: Context, imageView: ImageView, loadImage: (Uri, ImageView, BitmapCustomParams) -> Unit) {
        val fileName = URLUtil.guessFileName(webUrl, null, null)
        TaskExecutor.forBackgroundTasks()?.execute {
            val conn = URL(webUrl).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            try {
                conn.connect()
                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    if(!TaskExecutor.writingFiles.contains(fileName)){
                        TaskExecutor.writingFiles.add(fileName)
                        conn.inputStream.use {inputStream ->
                            val cacheFile = File(context.cacheDir, fileName)
                            val buffer = ByteArray(8*1024)
                            var len: Int
                            FileOutputStream(cacheFile).use {fos ->
                                Log.i("HttpFetcher", "$fileName writes")
                                while (inputStream.read(buffer).also { len = it } != -1) {
                                    fos.write(buffer, 0, len)
                                }
                                TaskExecutor.writingFiles.remove(fileName)
                            }
                            imageView.post {
                                val file = File(context.cacheDir, fileName)
                                if (file.exists()) {
                                    Log.i("HttpFetcher", "$fileName read ${file.length()} ${conn.contentLength}")
                                    loadImage(file.toUri(), imageView, BitmapCustomParams())
                                }

                            }
                        }
                    }

                }
                conn.disconnect()
            } catch (e: java.lang.Exception) {
                Log.e("HttpFetcher", e.stackTraceToString())
            }
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