package com.quangln2.customfeedui.data.models.datamodel

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.constants.ConstantSetup
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
){
    companion object{
        fun initializeUploadPost(context: Context): UploadPost{
            return UploadPost(
                feedId = UUID.randomUUID().toString(),
                name = context.resources.getString(R.string.account_name),
                avatar = ConstantSetup.AVATAR_LINK,
                createdTime = System.currentTimeMillis().toString(),
                )
        }
    }
}