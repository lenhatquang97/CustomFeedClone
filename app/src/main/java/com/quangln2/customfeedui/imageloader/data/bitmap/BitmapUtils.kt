package com.quangln2.customfeedui.imageloader.data.bitmap

import android.graphics.*
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import java.io.InputStream
import kotlin.math.max

object BitmapUtils {
    fun decodeBitmapFromInputStream(key: String, inputStream: InputStream, reqWidth: Int, reqHeight: Int, bmpParams: BitmapCustomParams): Bitmap? {
        val anotherOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        val actualKey = if(bmpParams.isFullScreen) "${key}_fullScreen" else key
        val reusedBitmap = LruBitmapCache.getLruCache(actualKey, bmpParams)
        if(reusedBitmap != null){
            inputStream.close()
            return reusedBitmap.getBitmap()
        } else {
            val bitmap = BitmapFactory.decodeStream(inputStream, null, anotherOptions)
            if(bitmap != null && !bitmap.isRecycled) {
                val resizedBitmap = resizeBitmap(bitmap, reqWidth, reqHeight)
                val managedBitmap = ManagedBitmap(resizedBitmap, resizedBitmap.width, resizedBitmap.height)
                LruBitmapCache.putIntoLruCache(actualKey, managedBitmap)
                inputStream.close()
                return resizedBitmap
            }
            inputStream.close()
        }
        return null
    }

    private fun resizeBitmap(bitmap: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap {
        val actualWidth = if(reqWidth > 0) reqWidth else bitmap.width
        val actualHeight = if(reqHeight > 0) reqHeight else bitmap.height
        var (dx, dy) = Pair(0f, 0f)
        val matrix = Matrix()

        val scale = max(actualWidth.toFloat() / bitmap.width, actualHeight.toFloat() / bitmap.height)
        if(bitmap.width * actualHeight > actualWidth * bitmap.height){
            dx = (actualWidth - bitmap.width * scale) * 0.5f
        } else {
            dy = (actualHeight - bitmap.height * scale) * 0.5f
        }
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx + 0.5f, dy + 0.5f)
        val result = Bitmap.createBitmap(actualWidth, actualHeight, bitmap.config)
        val canvas = Canvas(result)
        val paint = Paint()
        if(bitmap.width <= actualWidth && bitmap.height <= actualHeight){
            paint.isFilterBitmap = true
            paint.isAntiAlias = true
        }
        canvas.drawBitmap(bitmap, matrix, paint)
        bitmap.recycle()
        return result
    }
}