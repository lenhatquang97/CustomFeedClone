package com.quangln2.customfeedui.data.models.datamodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class OfflineResource(
    @SerializedName("url")
    @Expose
    var url: String,

    @SerializedName("size")
    @Expose
    var size: Long,

    @SerializedName("bytesCopied")
    @Expose
    var bytesCopied: Long,

    @SerializedName("stateOfDownloader")
    @Expose
    var stateOfDownloader: DownloadStatus
) {
    override fun equals(other: Any?): Boolean {
        if (other is OfflineResource) {
            val sameUrl = url == other.url
            val sameSize = size == other.size
            val sameBytesCopied = bytesCopied == other.bytesCopied
            val sameStateOfDownloader = stateOfDownloader == other.stateOfDownloader
            return sameUrl && sameSize && sameBytesCopied && sameStateOfDownloader
        }
        return false
    }
}
