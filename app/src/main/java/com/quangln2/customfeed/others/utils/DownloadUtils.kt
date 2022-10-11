package com.quangln2.customfeed.others.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.quangln2.customfeed.others.singleton.RetrofitSetup.downloadClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.*


object DownloadUtils {
    fun fileSizeFromInternet(url: String): Long {
        val request = Request.Builder().url(url).build()
        val response = downloadClient.newCall(request).execute()
        return response.body?.contentLength() ?: 0
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
            if (mimeType.contains("image")) {
                downloadImage(url, context)
            } else if (mimeType.contains("video")) {
                downloadVideo(url, context)
            }
        }
    }

    private fun downloadImage(imageUrl: String, context: Context) {
        val fileName = URLUtil.guessFileName(imageUrl, null, null)
        val file = File(context.cacheDir, fileName)
        Glide.with(context).load(imageUrl).into(
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    try {
                        val bitmap = (resource as BitmapDrawable).bitmap
                        val fout = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout)
                        fout.close()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Oh no", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}

            }
        )
    }

    private fun downloadVideo(videoUrl: String, context: Context) {
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
                }
            }

        })

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

    private fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
}