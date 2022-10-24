package com.quangln2.customfeed.data.constants

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object ConstantClass {
    const val NAME = "Quang Lê"
    const val AVATAR_LINK = "https://res.cloudinary.com/dnirizque/image/upload/v1666060641/samples/sheep.jpg"
    const val DEFAULT_ENDPOINT = "https://e4cf-2a09-bac1-7ac0-10-00-246-16.ap.ngrok.io/"
    const val PLEASE_ADD_CONTENT = "Please add some content"
    const val INTERNET_CONNECTED = "Internet has been connected"
    const val OFFLINE_MODE = "Offline mode"

    const val MAXIMUM_IMAGE_IN_A_GRID = 9

    //Anh bi mo
    val REQUEST_OPTIONS_WITH_SIZE_100 = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(100)
}