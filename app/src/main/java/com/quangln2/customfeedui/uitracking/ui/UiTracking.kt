package com.quangln2.customfeedui.uitracking.ui

import android.content.Context
import android.webkit.URLUtil
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import com.quangln2.customfeedui.imageloader.data.network.CodeUtils

object UiTracking {
    private fun getMaxMemory(): Long = Runtime.getRuntime().maxMemory() / 1048576L
    private fun getTotalMemory(): Long = Runtime.getRuntime().totalMemory() / 1048576L
    private fun getUsedMemory(): Long {
        val runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
    }
    fun formatString(avatarUrl: String, context: Context): String{
        val fileUri = CodeUtils.convertImageUrlToFileUriString(avatarUrl, context)
        val refCount = LruBitmapCache.getLruCacheWithoutIncreaseCount(fileUri)?.referenceCount
        return """
            Max Heap Size: ${getMaxMemory()} MB
            Total Heap Size: ${getTotalMemory()} MB
            Used Heap Size: ${getUsedMemory()} MB
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