package com.quangln2.customfeedui.imageloader.domain

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.quangln2.customfeedui.extensions.md5
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

    fun preCheckWithChecksum(actualPath: String, webUri: Uri, context: Context): Boolean{
        val checkSumParams = webUri.getQueryParameters("checksum")
        val checkSumValue = if (checkSumParams.isNotEmpty()) checkSumParams[0] else ""
        val fileContain = File(context.cacheDir, actualPath)
        if(fileContain.exists()){
            val actualChecksum = fileContain.md5()
            Log.d("ImageLoader", "Checksum on the server: $checkSumValue vs Checksum in reality: $actualChecksum")
            return checkSumValue == actualChecksum || checkSumValue.isEmpty()
        }
        return false
    }

}