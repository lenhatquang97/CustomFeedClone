package com.quangln2.customfeedui.imageloader.domain

import android.content.Context
import androidx.core.net.toUri
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import java.io.File

object ImageLoaderUtils {
    fun isInMemoryCache(fileName: String, context: Context): Boolean {
        val memoryCacheFile = File(context.cacheDir, fileName)
        return LruBitmapCache.containsKey(memoryCacheFile.toUri().toString())
    }
    fun doesFileExist(fileName: String, context: Context): Boolean {
        val file = File(context.cacheDir, fileName)
        return file.exists()
    }
    fun deleteIfExists(actualPath: String, context: Context){
        val fileContain = File(context.cacheDir, actualPath)
        if(fileContain.exists()){
            fileContain.delete()
        }
    }
    fun createFolder(folderName: String, context: Context){
        if(folderName.isNotEmpty()){
            val folderCreation = File(context.cacheDir, folderName)
            if(!folderCreation.exists())
                folderCreation.mkdir()
        }
    }

}