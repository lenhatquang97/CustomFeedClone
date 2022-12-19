package com.quangln2.customfeedui.imageloader.data.memcache

import android.util.Log
import android.util.LruCache
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.imageloader.data.bitmap.ManagedBitmap

object LruBitmapCache: CachePolicy {
    val bitmapToImageViewAddress = mutableMapOf<String, String>()

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

    fun containsKey(key: String): Boolean = memoryCache.get(key) != null

    fun getLruCache(key: String, bmpParams: BitmapCustomParams): ManagedBitmap? {
        val managedBitmap = memoryCache.get(key)
        if(bmpParams.countRef){
            managedBitmap?.addReferenceCount()
        }
        return managedBitmap
    }

    override fun getLruCacheWithoutIncreaseCount(key: String): ManagedBitmap? {
        return memoryCache.get(key)
    }

    override fun removeCache(key: String) {
        synchronized(memoryCache){
            val managedBitmap = memoryCache.get(key)
            Log.d("LruBitmapCache", "Before-after first ${managedBitmap?.referenceCount}")
            managedBitmap?.subtractReferenceCount()
            Log.d("ReferenceCount", "Reference count: ${managedBitmap?.referenceCount} $key")
            if(managedBitmap != null && managedBitmap.hasNoReference()){
                memoryCache.remove(key)
                managedBitmap.getBitmap().recycle()
            } else if(managedBitmap != null){
                Log.d("LruBitmapCache", "Before-after second ${managedBitmap.referenceCount}")
                putIntoLruCache(key, managedBitmap)
            }
        }
    }

    override fun removeAll() = memoryCache.evictAll()

    fun removeCacheForce(key: String){
        synchronized(memoryCache){
            val managedBitmap = memoryCache.get(key)
            if(managedBitmap != null && managedBitmap.hasNoReference()){
                memoryCache.remove(key)
                managedBitmap.getBitmap().recycle()
            }
        }
    }



}