package com.quangln2.customfeedui.imageloader.data.bitmap

import android.graphics.Bitmap

//TODO: Optimize reference count
class ManagedBitmap(private var bitmap: Bitmap, val width: Int = 0, val height: Int = 0, var referenceCount: Int = 1) {
    fun getBitmap(): Bitmap {
        return bitmap
    }
    fun addReferenceCount() = ++referenceCount
    fun subtractReferenceCount() = --referenceCount
    fun hasNoReference(): Boolean = referenceCount <= 0
}