package com.quangln2.customfeed.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quangln2.customfeed.data.converters.Converters
import com.quangln2.customfeed.data.models.datamodel.MyPost

@Database(entities = [MyPost::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FeedDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao

    companion object {
        @Volatile
        private var INSTANCE: FeedDatabase? = null
        fun getFeedDatabase(context: Context): FeedDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(context, FeedDatabase::class.java, "feed_database.db").build()
                }
            }
            return INSTANCE!!
        }
    }
}