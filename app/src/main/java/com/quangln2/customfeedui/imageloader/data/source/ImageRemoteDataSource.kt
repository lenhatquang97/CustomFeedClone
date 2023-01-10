package com.quangln2.customfeedui.imageloader.data.source

import android.content.Context
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.net.toUri
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.imageloader.data.network.HttpFetcher
import java.io.File

class ImageRemoteDataSource(private val context: Context, private val bmpParams: BitmapCustomParams) {
    fun downAndLoadImage(url: String, imageView: ImageView, width: Int, height: Int) {
        val httpFetcher = HttpFetcher(url)
        val loadImageCallback = fun(){
            imageView.post {
                val fileName = URLUtil.guessFileName(url, null, null)
                val actualPath = if(bmpParams.folderName.isEmpty()) fileName else "${bmpParams.folderName}/$fileName"
                val memoryCacheFile = File(context.cacheDir, actualPath)
                if (memoryCacheFile.exists()) {
                    val imageLocalDataSource = ImageLocalDataSource(context, bmpParams)
                    imageLocalDataSource.loadImageWithUri(memoryCacheFile.toUri(), imageView, width, height)
                }
            }
        }
        httpFetcher.downloadImage(context, bmpParams, loadImageCallback)
    }
}