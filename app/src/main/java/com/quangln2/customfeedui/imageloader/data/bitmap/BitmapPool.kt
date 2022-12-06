package com.quangln2.customfeedui.imageloader.data.bitmap

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.abs

object BitmapPool {
    private val bitmaps: MutableList<ManagedBitmap> = mutableListOf()
    fun getBitmap(reqWidth: Int, reqHeight: Int): ManagedBitmap{
        for(i in 0 until bitmaps.size){
            val bitmap = bitmaps[i]
            val widthDistance = abs(bitmap.getBitmap().width - reqWidth)
            val heightDistance = abs(bitmap.getBitmap().height - reqHeight)
            val isLessThanBitmap = reqWidth <= bitmap.getBitmap().width && reqHeight <= bitmap.getBitmap().height
            val isLessThanDistance = widthDistance <= 300 && heightDistance <= 300
            if(bitmap.getRefCount() == 0 && isLessThanBitmap && isLessThanDistance){
                bitmap.increaseRef()
                return bitmap
            }
        }
        val bitmap = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.RGB_565)
        val managedBitmap = ManagedBitmap(bitmap)
        return managedBitmap.apply {
            bitmaps.add(managedBitmap)
        }

    }

    fun recycle(imageView: ImageView){
        imageView.invalidate()
        val drawable = imageView.drawable
        val bmp = drawable.toBitmap()
        for(i in 0 until bitmaps.size){
            if(bitmaps[i].getBitmap() == bmp && bitmaps[i].getRefCount() > 0){
                bitmaps[i].decreaseRef()
            }
        }
    }

    fun createBitmapARGB8888(width: Int, height: Int): ManagedBitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val managedBitmap = ManagedBitmap(bitmap)
        return managedBitmap.apply {
            bitmaps.add(managedBitmap)
        }
    }
}