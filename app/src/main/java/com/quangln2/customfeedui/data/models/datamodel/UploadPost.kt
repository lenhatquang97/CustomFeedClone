package com.quangln2.customfeedui.data.models.datamodel

import android.content.Context
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.constants.ConstantSetup
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

data class UploadPost(
    var feedId: String = "",
    var name: String = "",
    var avatar: String = "",
    var createdTime: String = Date().time.toString(),
    var caption: String = "",
    var imagesAndVideos: MutableList<String> = mutableListOf(),
    var firstWidth: Int = 0,
    var firstHeight: Int = 0,
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
        fun uploadPostToJsonObject(obj: UploadPost): JSONObject{
            val jsonObject = JSONObject()
            val jsonResourceArray = JSONArray()
            obj.imagesAndVideos.forEach {
                jsonResourceArray.put(it)
            }

            jsonObject.apply {
                put("feedId", obj.feedId)
                put("name", obj.name)
                put("avatar", obj.avatar)
                put("createdTime", obj.createdTime)
                put("caption", obj.caption)
                put("imagesAndVideos", jsonResourceArray)
                put("firstWidth", obj.firstWidth)
                put("firstHeight", obj.firstHeight)
            }
            return jsonObject
        }

        private fun jsonStringToUploadPost(jsonString: String): UploadPost{
            val jsonObject = JSONObject(jsonString)
            val uploadPost = UploadPost()
            uploadPost.feedId = jsonObject.getString("feedId")
            uploadPost.name = jsonObject.getString("name")
            uploadPost.avatar = jsonObject.getString("avatar")
            uploadPost.createdTime = jsonObject.getString("createdTime")
            uploadPost.caption = jsonObject.getString("caption")
            uploadPost.firstWidth = jsonObject.getInt("firstWidth")
            uploadPost.firstHeight = jsonObject.getInt("firstHeight")
            val jsonResourceArray = if(jsonObject.getString("imagesAndVideos") != "null"){
                jsonObject.getJSONArray("imagesAndVideos")
            } else JSONArray()

            if(jsonResourceArray != null){
                for (i in 0 until jsonResourceArray.length()){
                    uploadPost.imagesAndVideos.add(jsonResourceArray.getString(i))
                }
            }

            return uploadPost
        }
        fun jsonStringToUploadPostList(jsonString: String): MutableList<UploadPost>{
            val jsonArray = JSONArray(jsonString)
            val uploadPostList = mutableListOf<UploadPost>()
            for (i in 0 until jsonArray.length()){
                uploadPostList.add(jsonStringToUploadPost(jsonArray.getString(i)))
            }
            return uploadPostList
        }
        fun uploadPostListToJsonString(uploadPostList: MutableList<UploadPost>): String{
            val jsonArray = JSONArray()
            uploadPostList.forEach {
                jsonArray.put(uploadPostToJsonObject(it))
            }
            return jsonArray.toString(4)
        }
    }
}