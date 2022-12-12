package com.quangln2.customfeedui.imageloader.data.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import com.quangln2.customfeedui.imageloader.data.diskcache.DiskCache
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

class BitmapUtils {
    fun emptyBitmap(context: Context): Bitmap {
        if(!LruBitmapCache.containsKey("emptyBmp")){
            val drawable = ColorDrawable(Color.parseColor("#aaaaaa"))
            val bmp = drawable.toBitmap(50, 50, Bitmap.Config.RGB_565)
            if(!bmp.isRecycled){
                LruBitmapCache.putIntoLruCache("emptyBmp", ManagedBitmap(bmp, width = 50, height = 50))
            }
        }
        return LruBitmapCache.getLruCache("emptyBmp")?.getBitmap()!!


    }
    private fun bitmapOptionsWithDensity(reqWidth: Int, reqHeight: Int, width: Int, height: Int): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        val ratio = calculateRatio(reqWidth, reqHeight, width, height, true)
        options.apply {
            inSampleSize = ratio
            inJustDecodeBounds = false
        }

        val maxWidth = max(reqWidth, width / options.inSampleSize)
        if(reqWidth != 0){
            options.inDensity = width
            options.inTargetDensity = maxWidth * options.inSampleSize
        }
        return options
    }



    fun decodeBitmapFromInputStream(key: String, inputStream: InputStream, reqWidth: Int, reqHeight: Int, context: Context): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        inputStream.mark(inputStream.available())
        BitmapFactory.decodeStream(inputStream, null, options)

        val (imageHeight, imageWidth) = Pair(options.outHeight, options.outWidth)
        val anotherOptions = bitmapOptionsWithDensity(reqWidth, reqHeight, imageWidth, imageHeight)
        val reusedBitmap = LruBitmapCache.getLruCache(key)
        if(reusedBitmap != null && !reusedBitmap.getBitmap().isRecycled){
            return reusedBitmap.getBitmap()
        } else {
            inputStream.reset()
            val bitmap = BitmapFactory.decodeStream(inputStream, null, anotherOptions)
            if(bitmap != null && !bitmap.isRecycled) {
                val managedBitmap = ManagedBitmap(bitmap, width = bitmap.width, height = bitmap.height)
                LruBitmapCache.putIntoLruCache(key, managedBitmap)
                DiskCache.writeBitmapToDiskCache(key, bitmap, context)

                inputStream.close()
                return bitmap
            }
            inputStream.close()
        }
        return emptyBitmap(context)
    }

    private fun calculateRatio(reqWidth: Int, reqHeight: Int, width: Int, height: Int, centerInside: Boolean): Int =
        if(height > reqHeight || width > reqWidth){
            val ratio: Int = if(reqHeight == 0 && reqWidth == 0){
                0
            }
            else if(reqHeight == 0){
                width / reqWidth
            } else if(reqWidth == 0) {
                height / reqHeight
            } else {
                val heightRatio = height / reqHeight
                val widthRatio = width / reqWidth
                if(centerInside){
                    max(heightRatio, widthRatio)
                } else {
                    min(heightRatio, widthRatio)
                }
            }
            if(ratio != 0) ratio else 1
        } else 1
}