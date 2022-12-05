package com.quangln2.customfeedui.others.extensions

import android.content.Context
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri

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