package com.quangln2.customfeedui.extensions

import android.content.Context
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor

fun ExecutorService.taskWaiting(): Int{
    val threadPoolExecutor = this as ThreadPoolExecutor
    return threadPoolExecutor.queue.size
}

fun Uri.getImageDimensions(context: Context): Pair<Int, Int> {
    val inputStream = context.contentResolver.openInputStream(this)!!
    val exif = ExifInterface(inputStream)

    val width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
    val height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)

    return Pair(width, height)
}
fun Uri.getVideoSize(context: Context): Pair<Int, Int> {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, this)
    val width =
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
    val height =
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
    retriever.release()
    return Pair(width, height)
}

fun File.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return this.inputStream().use { fis ->
        val buffer = ByteArray(8192)
        generateSequence {
            when (val bytesRead = fis.read(buffer)) {
                -1 -> null
                else -> bytesRead
            }
        }.forEach { bytesRead -> md.update(buffer, 0, bytesRead) }
        md.digest().joinToString("") { "%02x".format(it) }
    }
}