package com.quangln2.customfeedui.imageloader.data.bitmap

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import java.io.InputStream
import kotlin.math.max

class BitmapUtils {
    fun emptyBitmap(): Bitmap {
        if(!LruBitmapCache.containsKey("emptyBmp")){
            val drawable = ColorDrawable(Color.parseColor("#aaaaaa"))
            val bmp = drawable.toBitmap(50, 50, Bitmap.Config.RGB_565)
            if(!bmp.isRecycled){
                LruBitmapCache.putIntoLruCache("emptyBmp", ManagedBitmap(bmp, width = 50, height = 50))
            }
        }
        return LruBitmapCache.getLruCache("emptyBmp")?.getBitmap()!!
    }

    fun decodeBitmapFromInputStream(key: String, inputStream: InputStream, reqWidth: Int, reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        inputStream.mark(inputStream.available())
        BitmapFactory.decodeStream(inputStream, null, options)

        val anotherOptions = BitmapFactory.Options().apply { inJustDecodeBounds = false }
        val reusedBitmap = LruBitmapCache.getLruCache(key)
        if(reusedBitmap != null && !reusedBitmap.getBitmap().isRecycled){
            return reusedBitmap.getBitmap()
        } else {
            inputStream.reset()
            val bitmap = BitmapFactory.decodeStream(inputStream, null, anotherOptions)
            if(bitmap != null && !bitmap.isRecycled) {
                val resizedBitmap = resizeBitmap(bitmap, reqWidth, reqHeight)
                inputStream.close()
                return resizedBitmap
            }
            inputStream.close()
        }
        return emptyBitmap()
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