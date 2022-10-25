package com.quangln2.customfeed.data.constants

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object ConstantSetup {
    const val AVATAR_LINK = "https://res.cloudinary.com/dnirizque/image/upload/v1666060641/samples/sheep.jpg"
    const val DEFAULT_ENDPOINT = "https://996a-2a09-bac1-7a80-10-00-247-45.ap.ngrok.io/"
    val REQUEST_OPTIONS_WITH_SIZE_100 = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(100)

}
