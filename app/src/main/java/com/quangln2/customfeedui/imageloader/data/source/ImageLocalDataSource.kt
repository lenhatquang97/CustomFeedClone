package com.quangln2.customfeedui.imageloader.data.source

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapUtils
import com.quangln2.customfeedui.imageloader.data.network.HttpFetcher
import com.quangln2.customfeedui.uitracking.UiTracking
import kotlinx.coroutines.*
import java.io.File

class ImageLocalDataSource(private val context: Context, private val bmpParams: BitmapCustomParams) {
    fun handleCache(filePath: String, imageView: ImageView, width: Int, height: Int) {
        if(bmpParams.folderName.isNotEmpty()){
            val folderCreation = File(context.cacheDir, bmpParams.folderName)
            if(!folderCreation.exists())
                folderCreation.mkdir()
        }
        val memoryCacheFile = File(context.cacheDir, filePath)
        if (memoryCacheFile.exists()) {
            loadImageWithUri(memoryCacheFile.toUri(), imageView, width, height)
        }
    }

     fun loadImageWithUri(uri: Uri, imageView: ImageView, width: Int, height: Int) {
        val httpFetcher = HttpFetcher(uri)
        CoroutineScope(Dispatchers.Default).launch {
            Thread.currentThread().name = UiTracking.LOAD_WITH_URI + uri.toString()
            val inputStream = httpFetcher.fetchImageByInputStream(context)
            if (inputStream != null) {
                val bitmap = BitmapUtils.decodeBitmapFromInputStream(uri.toString(), inputStream, width, height, bmpParams)
                if (bitmap != null) {
                    withContext(Dispatchers.Main){
                        imageView.setImageBitmap(bitmap)
                    }
                    this.cancel()
                }
            }
        }
    }
}