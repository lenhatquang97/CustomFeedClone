package com.quangln2.customfeed.data.models.datamodel

import com.google.gson.annotations.SerializedName
import java.util.*

data class UploadPost(
    @SerializedName("feedId") var feedId: String = "",
    @SerializedName("name") var name: String = "",
    @SerializedName("avatar") var avatar: String = "",
    @SerializedName("createdTime") var createdTime: String = Date().time.toString(),
    @SerializedName("caption") var caption: String = "",
    @SerializedName("imagesAndVideos") var imagesAndVideos: MutableList<String> = mutableListOf(),
    @SerializedName("firstWidth") var firstWidth: Int = 0,
    @SerializedName("firstHeight") var firstHeight: Int = 0,
    //Transient
    @Transient var localPaths: MutableList<String> = mutableListOf()
)