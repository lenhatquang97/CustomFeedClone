package com.quangln2.customfeedui.imageloader.data.bitmap

import android.graphics.Bitmap.Config

data class CustomBitmapImage(
    val height: Int,
    val width: Int,
    val imageType: String,
    val bitmapConfig: Config
)