package com.quangln2.customfeedui.imageloader.data.memcache

import android.util.LruCache
import com.quangln2.customfeedui.imageloader.data.bitmap.ManagedBitmap

object LruBitmapCache: CachePolicy {
    val cacheSize = (Runtime.getRuntime().maxMemory() / 1024).toInt() / 12
    private var memoryCache: LruCache<String, ManagedBitmap> = object : LruCache<String, ManagedBitmap>(cacheSize){
        override fun sizeOf(key: String, value: ManagedBitmap): Int {
            return value.getBitmap().byteCount.div(1024)
        }
    }

    override fun putIntoLruCache(key: String, managedBitmap: ManagedBitmap){
        synchronized(memoryCache){
            if(memoryCache.get(key) == null){
                memoryCache.put(key, managedBitmap)
            }
        }
    }

    override fun getLruCache(key: String): ManagedBitmap?{
       return memoryCache.get(key)
    }

    override fun removeCache(key: String) {
        val managedBitmap = getLruCache(key)
        if(managedBitmap != null){
            managedBitmap.getBitmap().recycle()
            memoryCache.remove(key)
        }
    }




}