package com.quangln2.customfeedui.imageloader.data.bitmap

import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.abs

object BitmapPool {
    private val bitmaps: MutableList<ManagedBitmap> = mutableListOf()

    private fun findIndexToBeInserted(anotherManagedBitmap: ManagedBitmap, start: Int, end: Int): Int {
        val mid = (start + end) / 2
        if(mid >= end) return 0

        val k = anotherManagedBitmap.width * anotherManagedBitmap.height
        if (k >= bitmaps[mid].width * bitmaps[mid].height && k <= bitmaps[mid+1].width * bitmaps[mid+1].height) return mid + 1
        return if (bitmaps[mid].width * bitmaps[mid].height < k) findIndexToBeInserted(anotherManagedBitmap, mid + 1, end) else findIndexToBeInserted(anotherManagedBitmap, start, mid - 1)
    }

    fun getBitmap(reqWidth: Int, reqHeight: Int): ManagedBitmap{
        for(i in bitmaps.size - 1 downTo 0){
            val bitmap = bitmaps[i]
            if(bitmap != null){
                val widthDistance = abs(bitmap.getBitmap().width - reqWidth)
                val heightDistance = abs(bitmap.getBitmap().height - reqHeight)
                val isLessThanDistance = widthDistance <= 200 && heightDistance <= 200
                if(bitmap.getRefCount() == 0 && isLessThanDistance){
                    Log.i("BmpLog", i.toString())
                    bitmap.increaseRef()
                    return bitmap
                }
            }
        }
        val bitmap = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.RGB_565)
        val managedBitmap = ManagedBitmap(bitmap, width = reqWidth, height = reqHeight)
        val indexInserted = findIndexToBeInserted(managedBitmap, 0, bitmaps.size - 1)
        bitmaps.add(indexInserted, managedBitmap)
        return managedBitmap
    }

    fun recycle(imageView: ImageView){
        imageView.invalidate()
        val bmpRecycled = imageView.drawable.toBitmap()
        if(bmpRecycled != null){
            var firstPair = Pair(1000, 1000)
            var firstIndex = 0
            var secondPair = Pair(1000, 1000)
            var secondIndex = 0
            for(i in 0 until bitmaps.size){
                val widthDistance = abs(bitmaps[i].getBitmap().width - bmpRecycled.width)
                val heightDistance = abs(bitmaps[i].getBitmap().height - bmpRecycled.height)
                if(bitmaps[i] != null && bitmaps[i].getRefCount() > 0 && widthDistance <= 300 && heightDistance <= 300){
                    firstPair = Pair(widthDistance, heightDistance)
                    firstIndex = i
                    break
                }
            }
            for(i in bitmaps.size - 1 downTo 0){
                val widthDistance = abs(bitmaps[i].getBitmap().width - bmpRecycled.width)
                val heightDistance = abs(bitmaps[i].getBitmap().height - bmpRecycled.height)
                if(bitmaps[i] != null && bitmaps[i].getRefCount() > 0 && widthDistance <= 300 && heightDistance <= 300){
                    secondPair = Pair(widthDistance, heightDistance)
                    secondIndex = i
                    break
                }
            }
            if(firstPair.first + firstPair.second <= secondPair.first + secondPair.second){
                bitmaps[firstIndex].decreaseRef()
            } else {
                bitmaps[secondIndex].decreaseRef()
            }
            Log.d("BmpLog", "Bitmap size is ${bitmaps.size}")
        }

    }

    fun createBitmapARGB8888(width: Int, height: Int): ManagedBitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val managedBitmap = ManagedBitmap(bitmap, width = width, height = height)
        val indexInserted = findIndexToBeInserted(managedBitmap, 0, bitmaps.size - 1)
        bitmaps.add(indexInserted, managedBitmap)
        return managedBitmap
    }
}