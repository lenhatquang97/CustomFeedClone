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
    var resources: MutableList<OfflineResource>
){
    constructor(): this("","","","","", mutableListOf())
}
