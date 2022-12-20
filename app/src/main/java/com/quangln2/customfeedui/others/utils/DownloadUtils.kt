package com.quangln2.customfeedui.others.utils

import android.webkit.MimeTypeMap
import java.net.HttpURLConnection
import java.net.URL

object DownloadUtils {
    fun fileSizeFromInternet(url: String): Pair<Long, Exception?> {
        var value = 0L
        var exception: Exception? = null
        val obj = URL(url)
        val conn = obj.openConnection() as HttpURLConnection
        try {
            conn.connect()
            value = conn.contentLength.toLong()
            conn.disconnect()
        } catch (e: Exception) {
            exception = e
        }
        return Pair(value, exception)
    }

    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
}