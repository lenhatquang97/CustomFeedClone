package com.quangln2.customfeed.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.quangln2.customfeed.data.models.datamodel.MyPost

@Dao
interface FeedDao {
    @Query("SELECT * FROM my_post ORDER BY CAST(created_time AS BIGINT) DESC")
    fun getAll(): List<MyPost>

    @Insert
    fun insert(myPost: MyPost)

    @Update
    fun update(myPost: MyPost)

    @Query("DELETE FROM my_post WHERE feed_id = :id")
    fun delete(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM my_post WHERE feed_id = :feed_id)")
    fun existsWithId(feed_id: String): Int

    @Query("SELECT * FROM my_post WHERE feed_id = :feed_id")
    fun getFeedWithId(feed_id: String): MyPost
}