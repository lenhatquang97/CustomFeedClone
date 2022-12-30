package com.quangln2.customfeedui.imageloader.data.network

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.uitracking.ui.BitmapTaskManager
import com.quangln2.customfeedui.uitracking.ui.UiTracking
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    fun downloadImage(context: Context, bmpParams: BitmapCustomParams, loadImageCallback: () -> Unit){
        if(bmpParams.folderName.isNotEmpty()){
            val folderCreation = File(context.cacheDir, bmpParams.folderName)
            if(!folderCreation.exists())
                folderCreation.mkdir()
        }
        val fileName = URLUtil.guessFileName(webUrl, null, null)
        val actualPath = if(bmpParams.folderName.isEmpty()) fileName else "${bmpParams.folderName}/$fileName"
        val managingThread = Thread.getAllStackTraces().keys
        val doesContainLink = managingThread.any { it.name.contains(webUrl) && (it.state == Thread.State.RUNNABLE || it.state == Thread.State.WAITING) }
        val downloadImageTask = flow<String>{
            Thread.currentThread().name = UiTracking.THREAD_DOWNLOADING_IMAGE + webUrl
            val conn = withContext(Dispatchers.Main) {URL(webUrl).openConnection()} as HttpURLConnection
            conn.requestMethod = "GET"
            try{
                conn.connect()
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    if(!NetworkHelper.writingFiles.contains(actualPath)){
                        NetworkHelper.writingFiles.add(actualPath)
                        conn.inputStream.use {inputStream ->
                            val cacheFile = File(context.cacheDir, actualPath)
                            val buffer = ByteArray(8*1024)
                            var len: Int
                            FileOutputStream(cacheFile).use { fos ->
                                while (inputStream.read(buffer).also { len = it } != -1) {
                                    fos.write(buffer, 0, len)
                                    emit(actualPath)
                                }
                                NetworkHelper.writingFiles.remove(actualPath)
                                emit(actualPath)
                            }
                            emit(actualPath)
                        }
                    }
                }
                emit(actualPath)
                conn.disconnect()
            } catch (e: Exception){
                e.printStackTrace()
            }
        }.flowOn((BitmapTaskManager.executorDownloadingImage.asCoroutineDispatcher()))

        if(!doesContainLink){
            CoroutineScope(Dispatchers.Main).launch{
                downloadImageTask.collect {
                    if(!NetworkHelper.writingFiles.contains(it)){
                        loadImageCallback()
                    }
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