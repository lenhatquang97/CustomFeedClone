package com.quangln2.customfeedui.uitracking.ui

import android.content.Context
import android.webkit.URLUtil
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import com.quangln2.customfeedui.imageloader.data.network.NetworkHelper
import com.quangln2.customfeedui.uitracking.data.MemoryStats

object UiTracking {
    private val memStats = MemoryStats()
    fun formatString(avatarUrl: String, context: Context): String{
        val fileUri = NetworkHelper.convertImageUrlToFileUriString(avatarUrl, context)
        val refCount = LruBitmapCache.getLruCacheWithoutIncreaseCount(fileUri)?.referenceCount
        return """
            Max Heap Size: ${memStats.getMaxMemory()} MB
            Total Heap Size: ${memStats.getTotalMemory()} MB
            Used Heap Size: ${memStats.getUsedMemory()} MB
            Avatar Image RefCount: $refCount
        """.trimIndent()
    }
    fun getAllImageReferences(keyList: List<String>): String{
        val stringBuilder = StringBuilder()
        for(key in keyList){
            val managedBitmap = LruBitmapCache.getLruCacheWithoutIncreaseCount(key)
            if(managedBitmap != null){
                val fileName = URLUtil.guessFileName(key, null, null)
                stringBuilder.appendLine("Key: $fileName, Reference count: ${managedBitmap.referenceCount}")
            }
        }
        return stringBuilder.toString()
    }
}