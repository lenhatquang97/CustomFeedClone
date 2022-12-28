package com.quangln2.customfeedui.imageloader.data.network

object NetworkHelper {
    val writingFiles = mutableSetOf<String>()
    fun convertVideoUrlToImageUrl(videoUrl: String) : String{
        val index = videoUrl.lastIndexOf(".mp4")
        if(index != -1){
            return videoUrl.substring(0, index) + ".png"
        }
        return videoUrl
    }
}