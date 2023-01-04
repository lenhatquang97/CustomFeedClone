package com.quangln2.customfeedui.data.models.datamodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

@Entity(tableName = "my_post")
data class MyPost(
    @PrimaryKey @ColumnInfo(name = "feed_id") var feedId: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "avatar") var avatar: String,
    @ColumnInfo(name = "created_time") var createdTime: String,
    @ColumnInfo(name = "caption") var caption: String,
    @ColumnInfo(name = "resources") var resources: MutableList<OfflineResource>,
    @ColumnInfo(name = "firstWidth") var firstWidth: Int,
    @ColumnInfo(name = "firstHeight") var firstHeight: Int,
) {
    constructor() : this(UUID.randomUUID().toString(), "", "", "", "", mutableListOf(), 0, 0)

    override fun equals(other: Any?): Boolean {
        if (other is MyPost) {
            val sameFeedId = feedId == other.feedId
            val sameName = name == other.name
            val sameAvatar = avatar == other.avatar
            val sameCreatedTime = createdTime == other.createdTime
            val sameCaption = caption == other.caption
            val sameResources = resources.size == other.resources.size
            val sameFirstWidth = firstWidth == other.firstWidth
            val sameFirstHeight = firstHeight == other.firstHeight

            for ((a, b) in resources.zip(other.resources)) {
                if (a != b) {
                    return false
                }
            }
            return sameFeedId && sameName && sameAvatar && sameCreatedTime && sameCaption && sameResources && sameFirstWidth && sameFirstHeight
        }
        return false
    }
    companion object{
        private fun toJson(obj: MyPost): JSONObject{
            val jsonObject = JSONObject()
            val jsonResourceArray = JSONArray()
            obj.resources.forEach {
                jsonResourceArray.put(OfflineResource.offlineResourceToJsonObject(it))
            }
            jsonObject.apply {
                put("feedId", obj.feedId)
                put("name", obj.name)
                put("avatar", obj.avatar)
                put("createdTime", obj.createdTime)
                put("caption", obj.caption)
                put("resources", jsonResourceArray)
                put("firstWidth", obj.firstWidth)
                put("firstHeight", obj.firstHeight)
            }

            return jsonObject
        }
        private fun fromJson(json: String): MyPost{
            val jsonObject = JSONObject(json)
            val offlineResources = mutableListOf<OfflineResource>()
            val jsonResourceArray = jsonObject.getJSONArray("resources")
            for(i in 0 until jsonResourceArray.length()){
                offlineResources.add(OfflineResource.jsonStringToOfflineResource(jsonResourceArray.getString(i)))
            }

            return MyPost(
                jsonObject.getString("feedId"),
                jsonObject.getString("name"),
                jsonObject.getString("avatar"),
                jsonObject.getString("createdTime"),
                jsonObject.getString("caption"),
                offlineResources,
                jsonObject.getInt("firstWidth"),
                jsonObject.getInt("firstHeight")
            )
        }
        fun listToJsonString(obj: List<MyPost>): String {
            val jsonArray = JSONArray()
            obj.forEach {
                jsonArray.put(toJson(it))
            }
            return jsonArray.toString()
        }
        fun jsonStringToList(json: String): List<MyPost> {
            val jsonArray = JSONArray(json)
            val list = mutableListOf<MyPost>()
            for(i in 0 until jsonArray.length()){
                val jsonObject = jsonArray.getJSONObject(i)
                list.add(fromJson(jsonObject.toString()))
            }
            return list
        }
    }
}
