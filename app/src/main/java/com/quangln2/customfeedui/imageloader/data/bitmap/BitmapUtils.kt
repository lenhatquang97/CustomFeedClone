package com.quangln2.customfeedui.imageloader.data.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

class BitmapUtils {
    fun emptyBitmap(): Bitmap {
        val drawable = ColorDrawable(Color.parseColor("#aaaaaa"))
        return drawable.toBitmap(50, 50, Bitmap.Config.RGB_565)
    }
    fun decodeBitmapFromInputStream(inputStream: InputStream, reqWidth: Int, reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options()

        inputStream.mark(inputStream.available())
        BitmapFactory.decodeStream(inputStream, null, options)
        val (imageHeight, imageWidth) = Pair(options.outHeight, options.outWidth)
        val ratio = calculateRatio(reqWidth, reqHeight, imageWidth, imageHeight, true)
        val maxWidth = max(reqWidth, imageWidth / ratio)
        val maxHeight = max(reqHeight, imageHeight / ratio)

        options.inSampleSize = ratio
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.outWidth = maxWidth
        options.outHeight = maxHeight
        options.inBitmap = BitmapPool.getBitmap(maxWidth, maxHeight).getBitmap()
        return try{
            inputStream.reset()
            val drawable = ColorDrawable(Color.parseColor("#aaaaaa"))
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            return bitmap?: drawable.toBitmap(50, 50, Bitmap.Config.RGB_565)
        } catch (e: java.lang.Exception){
            inputStream.reset()
            options.inBitmap = BitmapPool.createBitmapARGB8888(maxWidth, maxHeight).getBitmap()
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            val drawable = ColorDrawable(Color.parseColor("#aaaaaa"))
            return bitmap?: drawable.toBitmap(50, 50, Bitmap.Config.RGB_565)
        }
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