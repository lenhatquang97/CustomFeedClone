package com.quangln2.customfeed.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_post")
data class MyPost(
    @PrimaryKey @ColumnInfo(name="feed_id") var feedId: String,
    @ColumnInfo(name="name") var name: String,
    @ColumnInfo(name="avatar") var avatar: String,
    @ColumnInfo(name="created_time") var createdTime: String,
    @ColumnInfo(name="caption") var caption: String,
    @ColumnInfo(name="resources") var resources: MutableList<OfflineResource>
){
    constructor(): this("","","","","", mutableListOf())

    override fun equals(other: Any?): Boolean {
        if(other is MyPost){
            val sameFeedId = feedId == other.feedId
            val sameName = name == other.name
            val sameAvatar = avatar == other.avatar
            val sameCreatedTime = createdTime == other.createdTime
            val sameCaption = caption == other.caption
            val sameResources = resources.size == other.resources.size
            for((a,b) in resources.zip(other.resources)){
                if(a != b){
                    return false
                }
            }
            return sameFeedId && sameName && sameAvatar && sameCreatedTime && sameCaption && sameResources
        }
        return false
    }
}
