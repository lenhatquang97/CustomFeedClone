package com.quangln2.customfeedui.data.models.datamodel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
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

    override fun hashCode(): Int {
        return super.hashCode()
    }

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
}
