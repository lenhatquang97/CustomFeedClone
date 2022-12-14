package com.quangln2.customfeedui.others.utils

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast
import okhttp3.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object DownloadUtils {
    fun fileSizeFromInternet(url: String): Pair<Long, Exception?> {
        var value = 0L
        var exception: Exception? = null
        val obj = URL(url)
        val conn = obj.openConnection() as HttpURLConnection
        try {
            conn.connect()
            value = conn.contentLength.toLong()
            conn.disconnect()
        } catch (e: Exception) {
            exception = e
        }
        return Pair(value, exception)
    }

    fun downloadResource(url: String, context: Context) {
        if (doesLocalFileExist(url, context)) return
        val mimeType = getMimeType(url)
        if (mimeType != null) {
            //Only download video
            if (mimeType.contains("video")) {
                downloadVideo(url, context)
            }
        }
    }

    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    private fun doesLocalFileExist(url: String, context: Context): Boolean {
        val fileName = URLUtil.guessFileName(url, null, null)
        val file = File(context.cacheDir, fileName)
        return file.exists()
    }

    private fun write(inputStream: InputStream?, outputStream: FileOutputStream): Long {
        BufferedInputStream(inputStream).use { input ->
            val dataBuffer = ByteArray(4 * 1024)
            var readBytes: Int
            var totalBytes: Long = 0
            while (input.read(dataBuffer).also { readBytes = it } != -1) {
                Log.d("DownloadUtilsTag", "write: $totalBytes")
                totalBytes += readBytes.toLong()
                outputStream.write(dataBuffer, 0, readBytes)
            }
            return totalBytes
        }
    }

    //Think for implementing HttpUrlConnection instead of OkHttp
    private fun downloadVideo(videoUrl: String, context: Context) {
        try {
            val req = Request.Builder().url(videoUrl).build()
            val fileName = URLUtil.guessFileName(videoUrl, null, null)
            val file = File(context.cacheDir, fileName)
            OkHttpClient().newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val fileOut = FileOutputStream(file)
                        write(response.body!!.byteStream(), fileOut)
                        fileOut.close()
                        response.close()
                    } else {
                        Toast.makeText(context, "Oh no!!!", Toast.LENGTH_SHORT).show()
                        response.close()
                    }
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}