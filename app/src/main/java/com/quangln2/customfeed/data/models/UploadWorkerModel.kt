package com.quangln2.customfeed.data.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UploadWorkerModel(
    @SerializedName("caption")
    @Expose
    val caption: String,

    @SerializedName("uriLists")
    @Expose
    val uriLists: List<String>
)