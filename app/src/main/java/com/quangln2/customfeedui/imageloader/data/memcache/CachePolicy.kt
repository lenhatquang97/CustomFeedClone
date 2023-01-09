package com.quangln2.customfeedui.imageloader.data.memcache

import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.imageloader.data.bitmap.ManagedBitmap

interface CachePolicy {
    fun containsKey(key: String): Boolean
    fun putIntoLruCache(key: String, managedBitmap: ManagedBitmap)
    fun getLruCache(key: String, bmpParams: BitmapCustomParams): ManagedBitmap?
    fun getLruCacheWithoutIncreaseCount(key: String): ManagedBitmap?
    fun removeCache(key: String)
    fun removeCacheForce(key: String)
    fun removeAll()

}