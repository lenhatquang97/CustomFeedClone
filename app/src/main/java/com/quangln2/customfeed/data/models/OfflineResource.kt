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

    @SerializedName("downloadProgress")
    @Expose
    var downloadProgress: Int,

    @SerializedName("stateOfDownloader")
    @Expose
    var stateOfDownloader: DownloadStatus
)
