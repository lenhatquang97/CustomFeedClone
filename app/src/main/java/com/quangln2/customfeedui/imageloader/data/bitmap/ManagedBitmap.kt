package com.quangln2.customfeedui.imageloader.data.bitmap

import android.graphics.Bitmap

class ManagedBitmap(private var bitmap: Bitmap, val width: Int = 0, val height: Int = 0) {
    fun getBitmap(): Bitmap {
        return bitmap
    }
}