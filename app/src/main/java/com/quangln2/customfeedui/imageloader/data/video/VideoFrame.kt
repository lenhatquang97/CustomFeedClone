package com.quangln2.customfeedui.imageloader.data.video

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache

object VideoFrame {
     fun bitmapFrameFromLocalVideo(uri: Uri, context: Context, bmpParams: BitmapCustomParams): Bitmap? {
         val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
         val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
         cursor?.apply {
             moveToFirst()
             val columnIndex = getColumnIndex(filePathColumn[0])
             val picturePath = getString(columnIndex)
             close()
             val oldBmp = LruBitmapCache.getLruCache(uri.toString(), bmpParams)
             return oldBmp?.getBitmap() ?: ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
         }
         return null
     }
}