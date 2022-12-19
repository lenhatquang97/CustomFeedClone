package com.quangln2.customfeedui.imageloader.data.network

import android.content.Context
import android.webkit.URLUtil
import androidx.core.net.toUri
import java.io.File

object NetworkHelper {
    fun convertVideoUrlToImageUrl(videoUrl: String) : String{
        val index = videoUrl.lastIndexOf(".mp4")
        if(index != -1){
            return videoUrl.substring(0, index) + ".png"
        }
        return videoUrl
    }
    fun convertImageUrlToFileUriString(webUrl: String, context: Context): String {
        val imageThumbnailUrl = convertVideoUrlToImageUrl(webUrl)
        val fileName = URLUtil.guessFileName(imageThumbnailUrl, null, null)
        val convertToUri = File(context.cacheDir, fileName)
        return convertToUri.toUri().toString()
    }
    fun cacheName(webUrlOrFileUri: String, widthAndHeight: Pair<Int, Int>): String {
        return "${webUrlOrFileUri}_${widthAndHeight.first}_${widthAndHeight.second}"
    }
}