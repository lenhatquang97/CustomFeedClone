package com.quangln2.customfeedui.data.constants

import android.content.res.Resources
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object ConstantSetup {
    //REST API
    private const val DEFAULT_ENDPOINT = "https://84d3-171-244-166-188.ap.ngrok.io/"
    const val GET_FEEDS = DEFAULT_ENDPOINT + "feeds"
    const val UPLOAD_FEED_VERSION_2 = DEFAULT_ENDPOINT + "feeds/upload_v2"
    //Note: need id: feeds/{id}
    const val DELETE_FEED = DEFAULT_ENDPOINT + "feeds/"
    //Hardcoded avatar link
    const val AVATAR_LINK = "https://res.cloudinary.com/dnirizque/image/upload/v1666060641/samples/sheep.jpg"
    //Max items in a grid
    const val MAXIMUM_IMAGE_IN_A_GRID = 9
    //Glide setup
    val REQUEST_OPTIONS_WITH_SIZE_100 = RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).format(DecodeFormat.PREFER_RGB_565).override(100)
    val REQUEST_WITH_RGB_565 = RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).format(DecodeFormat.PREFER_RGB_565).override(500)
    //Phone information
    val PHONE_HEIGHT = Resources.getSystem().displayMetrics.heightPixels
}
