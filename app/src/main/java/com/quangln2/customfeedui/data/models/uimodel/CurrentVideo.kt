package com.quangln2.customfeedui.data.models.uimodel

import android.os.Bundle

data class CurrentVideo(
    val currentVideoPosition: Long,
    val url: String,
    val listOfUrls: ArrayList<String>
){
    fun encapsulateToBundle(): Bundle = Bundle().apply {
        putLong("currentVideoPosition", currentVideoPosition)
        putString("value", url)
        putStringArrayList("listOfUrls", listOfUrls)
    }
}
