package com.quangln2.customfeedui.imageloader.data.memcache

import android.util.Log
import android.util.LruCache
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.imageloader.data.bitmap.ManagedBitmap
import java.lang.ref.WeakReference

object LruBitmapCache: CachePolicy {
    val bitmapToImageViewAddress = mutableMapOf<String, String>()

    val cacheSize = (Runtime.getRuntime().maxMemory() / 1024).toInt() / 12

    private var memoryCache: LruCache<String, WeakReference<ManagedBitmap>> = object : LruCache<String, WeakReference<ManagedBitmap>>(cacheSize){
        override fun sizeOf(key: String, value: WeakReference<ManagedBitmap>): Int {
            val managedBitmap = value.get()
            return managedBitmap?.getBitmap()?.byteCount?.div(1024) ?: 100
        }

    }

    override fun putIntoLruCache(key: String, managedBitmap: ManagedBitmap){
        synchronized(memoryCache){
            if(memoryCache.get(key) == null){
                memoryCache.put(key, WeakReference(managedBitmap))
            }
        }
    }

    fun containsKey(key: String): Boolean = memoryCache.get(key) != null

    fun getLruCache(key: String, bmpParams: BitmapCustomParams): ManagedBitmap? {
        val tmpBmp = memoryCache.get(key)
        val managedBitmap = tmpBmp?.get()
        if(bmpParams.countRef){
            managedBitmap?.addReferenceCount()
        }
        return managedBitmap
    }

    override fun getLruCacheWithoutIncreaseCount(key: String): ManagedBitmap? {
        return if(containsKey(key))
            memoryCache.get(key).get()
        else null
    }

    override fun removeCache(key: String) {
        synchronized(memoryCache){
            val tmpBmp = memoryCache.get(key)
            val managedBitmap = tmpBmp?.get()
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
            val managedBitmap = memoryCache.get(key).get()
            if(managedBitmap != null && managedBitmap.hasNoReference()){
                memoryCache.remove(key)
                managedBitmap.getBitmap().recycle()
            }
        }
    }



}