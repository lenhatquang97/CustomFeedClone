package com.quangln2.customfeed.others.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.webkit.URLUtil
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import okhttp3.*
import java.io.*

object DownloadUtils {
    fun downloadImage(imageUrl: String, context: Context){
        val fileName = URLUtil.guessFileName(imageUrl, null, null)
        val file = File(context.filesDir, fileName)
        Glide.with(context).load(imageUrl).into(
            object : CustomTarget<Drawable>(){
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    try{
                        val bitmap = (resource as BitmapDrawable).bitmap
                        val fout = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout)
                        fout.close()
                    } catch (e: Exception){
                        Toast.makeText(context, "Oh no", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}

            }
        )
    }

    fun downloadVideo(videoUrl: String, context: Context){
        val client = OkHttpClient()
        val req = Request.Builder().url(videoUrl).build()
        val fileName = URLUtil.guessFileName(videoUrl, null, null)
        val file = File(context.filesDir, fileName)
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful){
                    val fout = FileOutputStream(file)
                    write(response.body!!.byteStream(), fout)
                    fout.close()
                } else {
                    Toast.makeText(context, "Oh no!!!", Toast.LENGTH_SHORT).show()
                }
            }

        })

    }

    fun write(inputStream: InputStream?, outputStream: FileOutputStream): Long {
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
}