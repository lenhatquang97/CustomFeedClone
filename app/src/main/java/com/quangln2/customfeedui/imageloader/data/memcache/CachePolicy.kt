package com.quangln2.customfeedui.imageloader.data.memcache

import com.quangln2.customfeedui.imageloader.data.bitmap.ManagedBitmap

interface CachePolicy {
    fun putIntoLruCache(key: String, managedBitmap: ManagedBitmap)
    fun getLruCacheWithoutIncreaseCount(key: String): ManagedBitmap?
    fun removeCache(key: String)
    fun removeAll()

}