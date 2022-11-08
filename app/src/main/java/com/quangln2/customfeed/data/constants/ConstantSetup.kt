package com.quangln2.customfeed.data.constants

import android.content.res.Resources
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object ConstantSetup {
    const val AVATAR_LINK = "https://res.cloudinary.com/dnirizque/image/upload/v1666060641/samples/sheep.jpg"
    const val DEFAULT_ENDPOINT = "https://59fa-2a09-bac0-23-00-827-7f55.ap.ngrok.io/"
    val REQUEST_OPTIONS_WITH_SIZE_100 = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).format(DecodeFormat.PREFER_RGB_565).override(100)
    val PHONE_HEIGHT = Resources.getSystem().displayMetrics.heightPixels
    const val MAXIMUM_IMAGE_IN_A_GRID = 9
}
