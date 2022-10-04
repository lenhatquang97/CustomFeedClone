package com.quangln2.customfeed.data.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class OfflineResource(
    @SerializedName("url")
    @Expose
    var url: String,

    @SerializedName("localPath")
    @Expose
    var localPath: String,

    @SerializedName("size")
    @Expose
    var size: Long,

    @SerializedName("bytesCopied")
    @Expose
    var bytesCopied: Long,

    @SerializedName("stateOfDownloader")
    @Expose
    var stateOfDownloader: DownloadStatus
)
