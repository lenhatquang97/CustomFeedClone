package com.quangln2.customfeed.data.database

import androidx.room.*
import com.quangln2.customfeed.data.models.MyPost
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM my_post")
    fun getAll(): Flow<List<MyPost>>

    @Insert
    fun insert(myPost: MyPost)

    @Update
    fun update(myPost: MyPost)

    @Delete
    fun delete(myPost: MyPost)
}