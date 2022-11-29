package com.quangln2.customfeedui.data.constants

import android.content.res.Resources
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object ConstantSetup {
    const val AVATAR_LINK = "https://res.cloudinary.com/dnirizque/image/upload/v1666060641/samples/sheep.jpg"
    const val DEFAULT_ENDPOINT = "https://b707-171-244-166-189.ap.ngrok.io/"
    const val MAXIMUM_IMAGE_IN_A_GRID = 9
    val REQUEST_OPTIONS_WITH_SIZE_100 = RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).format(DecodeFormat.PREFER_RGB_565).override(100)
    val REQUEST_WITH_RGB_565 = RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).format(DecodeFormat.PREFER_RGB_565).override(500)
    val PHONE_HEIGHT = Resources.getSystem().displayMetrics.heightPixels
}
