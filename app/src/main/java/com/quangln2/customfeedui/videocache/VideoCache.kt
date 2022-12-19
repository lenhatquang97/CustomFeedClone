package com.quangln2.customfeedui.videocache

import android.content.Context
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.*
import java.io.File


object VideoCache{
    private var downloadCache: Cache? = null

    fun buildCacheDataSourceFactory(context: Context): DataSource.Factory {
        val cache = getDownloadCache(context)
        val cacheSink = CacheDataSink.Factory()
            .setCache(cache)
        val upstreamFactory = DefaultDataSource.Factory(context, DefaultHttpDataSource.Factory())
        return CacheDataSource.Factory()
            .setCache(cache)
            .setCacheWriteDataSinkFactory(cacheSink)
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
    @Synchronized
    private fun getDownloadCache(context: Context): Cache {
        if (downloadCache == null) {
            val downloadContentDirectory = File(context.filesDir, "downloads")
            downloadCache = SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), StandaloneDatabaseProvider(context))
        }
        return downloadCache!!
    }
}