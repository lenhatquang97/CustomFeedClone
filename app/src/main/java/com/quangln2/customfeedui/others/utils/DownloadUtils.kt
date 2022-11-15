package com.quangln2.customfeedui.others.utils

import android.content.Context
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast
import com.quangln2.customfeedui.others.singleton.RetrofitSetup.downloadClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.*


object DownloadUtils {
    fun fileSizeFromInternet(url: String): Pair<Long, Exception?> {
        var value = 0L
        var exception: Exception? = null
        try {
            val request = Request.Builder().url(url).build()
            val response = downloadClient.newCall(request).execute()
            value = response.body?.contentLength() ?: 0L
            response.close()
        } catch (e: Exception) {
            exception = e
        } finally {
            return Pair(value, exception)
        }
    }

    fun doesLocalFileExist(url: String, context: Context): Boolean {
        val fileName = URLUtil.guessFileName(url, null, null)
        val file = File(context.cacheDir, fileName)
        return file.exists()
    }

    fun isValidFile(url: String, context: Context, anotherSize: Long): Boolean {
        val fileName = URLUtil.guessFileName(url, null, null)
        val file = File(context.cacheDir, fileName)
        if (!file.exists()) return false
        try {
            if (file.length() == anotherSize && anotherSize != 0L) {
                return true
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getTemporaryFilePath(url: String, context: Context): String {
        val fileName = URLUtil.guessFileName(url, null, null)
        val file = File(context.cacheDir, fileName)
        return file.absolutePath
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

    private fun downloadVideo(videoUrl: String, context: Context) {
        try {
            val req = Request.Builder().url(videoUrl).build()
            val fileName = URLUtil.guessFileName(videoUrl, null, null)
            val file = File(context.cacheDir, fileName)
            downloadClient.newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val fout = FileOutputStream(file)
                        write(response.body!!.byteStream(), fout)
                        fout.close()
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

    fun downloadVideoSynchronous(videoUrl: String, context: Context){
        try {
            val req = Request.Builder().url(videoUrl).build()
            val fileName = URLUtil.guessFileName(videoUrl, null, null)
            val file = File(context.cacheDir, fileName)
            val response = downloadClient.newCall(req).execute()
            if (response.isSuccessful) {
                val fout = FileOutputStream(file)
                write(response.body!!.byteStream(), fout)
                fout.close()
                response.close()
            } else {
                Toast.makeText(context, "Oh no!!!", Toast.LENGTH_SHORT).show()
                response.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun write(inputStream: InputStream?, outputStream: FileOutputStream): Long {
        BufferedInputStream(inputStream).use { input ->
            val dataBuffer = ByteArray(4 * 1024)
            var readBytes: Int
            var totalBytes: Long = 0
            while (input.read(dataBuffer).also { readBytes = it } != -1) {
                totalBytes += readBytes.toLong()
                outputStream.write(dataBuffer, 0, readBytes)
            }
            return totalBytes
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


}