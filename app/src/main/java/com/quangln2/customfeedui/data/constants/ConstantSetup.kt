package com.quangln2.customfeedui.data.constants

import android.content.res.Resources

object ConstantSetup {
    //REST API
    /*
    * Get feeds API: /feeds
    * Upload file (v1): /upload
    * Upload a post (v2): /upload_v2
    * Delete Feed: /feeds/{id}
    */
    private const val DEFAULT_ENDPOINT = "https://feeduiclone.win/"

    const val GET_FEEDS = DEFAULT_ENDPOINT + "feeds"
    const val UPLOAD_FEED_VERSION_2 = DEFAULT_ENDPOINT + "feeds/upload_v2"
    const val DELETE_FEED = DEFAULT_ENDPOINT + "feeds/"
    const val UPLOAD_FILE = DEFAULT_ENDPOINT + "upload"

    //Hardcoded avatar link
    const val AVATAR_LINK = "https://i.imgur.com/CzXTtJV.jpg"
    //Max items in a grid
    const val MAXIMUM_IMAGE_IN_A_GRID = 9
    //Phone information
    val PHONE_HEIGHT = Resources.getSystem().displayMetrics.heightPixels
    val PHONE_WIDTH = Resources.getSystem().displayMetrics.widthPixels
}
