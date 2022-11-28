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
    var bytesCopied: Long
) {
    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is OfflineResource) {
            val sameUrl = url == other.url
            val sameSize = size == other.size
            val sameBytesCopied = bytesCopied == other.bytesCopied
            return sameUrl && sameSize && sameBytesCopied
        }
        return false
    }
}
