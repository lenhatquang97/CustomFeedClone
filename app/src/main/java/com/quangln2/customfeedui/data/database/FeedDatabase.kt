package com.quangln2.customfeedui.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quangln2.customfeedui.data.converters.Converters
import com.quangln2.customfeedui.data.models.datamodel.MyPost

@Database(entities = [MyPost::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FeedDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao

    companion object {
        @Volatile
        private var INSTANCE: FeedDatabase? = null
        fun getFeedDatabase(context: Context, databaseName: String = "feed_database.db"): FeedDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(context, FeedDatabase::class.java, databaseName).build()
                }
            }
            return INSTANCE!!
        }
    }
}