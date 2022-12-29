package com.quangln2.customfeedui.imageloader.data.network

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import android.widget.ImageView
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.uitracking.ui.BitmapTaskManager
import com.quangln2.customfeedui.uitracking.ui.UiTracking
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

    fun downloadImage(context: Context, imageView: ImageView, loadImage: (String, ImageView, BitmapCustomParams) -> Unit, bmpParams: BitmapCustomParams) {
        if(bmpParams.folderName.isNotEmpty()){
            val folderCreation = File(context.cacheDir, bmpParams.folderName)
            if(!folderCreation.exists())
                folderCreation.mkdir()
        }

        val fileName = URLUtil.guessFileName(webUrl, null, null)
        val actualPath = if(bmpParams.folderName.isEmpty()) fileName else "${bmpParams.folderName}/$fileName"
        val managingThread = Thread.getAllStackTraces().keys
        val doesContainLink = managingThread.any { it.name.contains(webUrl) && (it.state == Thread.State.RUNNABLE || it.state == Thread.State.WAITING) }
        if(!doesContainLink){
            BitmapTaskManager.executorDownloadingImage.execute {
                Thread.currentThread().name = UiTracking.THREAD_DOWNLOADING_IMAGE + webUrl
                val conn = URL(webUrl).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                try {
                    conn.connect()
                    val responseCode = conn.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        if(!NetworkHelper.writingFiles.contains(actualPath)){
                            NetworkHelper.writingFiles.add(actualPath)
                            conn.inputStream.use {inputStream ->
                                val cacheFile = File(context.cacheDir, actualPath)
                                val buffer = ByteArray(8*1024)
                                var len: Int
                                FileOutputStream(cacheFile).use { fos ->
                                    while (inputStream.read(buffer).also { len = it } != -1) {
                                        fos.write(buffer, 0, len)
                                    }
                                    NetworkHelper.onAfterRemove(actualPath){
                                        println("OnAfterRemove A: $actualPath")
                                        loadImage(actualPath, imageView, bmpParams)
                                    }
                                }
                                if(!NetworkHelper.writingFiles.contains(actualPath)){
                                    println("OnAfterRemove B: $actualPath")
                                    loadImage(actualPath, imageView, bmpParams)
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