package com.quangln2.customfeedui.imageloader.data.diskcache
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/*
* Main purpose of managedTmpMap is to check whether file is valid. If file not valid (means different MD5 codes), updates again
* */

object DiskCache {
    private val managedWrite = mutableSetOf<String>()

    fun containsWith(key: String, context: Context): Boolean{
        val md5Key = md5Hash(key)
        return File(context.cacheDir, md5Key).exists()
    }

    fun writeBitmapToDiskCache(key: String, bitmap: Bitmap, context: Context){
        val md5Key = md5Hash(key)
        val cacheFile = File(context.cacheDir, md5Key)
        if(!cacheFile.exists()){
            val anotherCacheFile = File(context.cacheDir, md5Key)
            anotherCacheFile.createNewFile()
            managedWrite.add(md5Key)
            BufferedOutputStream(anotherCacheFile.outputStream(), 8192).use {
                ObjectOutputStream(it).use {objOut ->
                    writeObject(objOut, bitmap)
                }
            }
            managedWrite.remove(md5Key)
        } else {
            val anotherCacheFile = File(context.cacheDir, md5Key)
            val oldSize = anotherCacheFile.length()
            val newSize = bitmap.byteCount
            if(oldSize < newSize && !managedWrite.contains(md5Key)){
                anotherCacheFile.delete()
                BufferedOutputStream(anotherCacheFile.outputStream(), 8192).use {
                    ObjectOutputStream(it).use {objOut ->
                        writeObject(objOut, bitmap)
                    }
                }
            }
        }
    }

    fun getBitmapFromDiskCache(key: String, context: Context): Bitmap?{
        val md5Key = md5Hash(key)
        val cacheFile = File(context.cacheDir, md5Key)
        if(cacheFile.exists()){
            val anotherCacheFile = File(context.cacheDir, md5Key)
            BufferedInputStream(anotherCacheFile.inputStream(), 8192).use {
                ObjectInputStream(it).use {objIn ->
                    return readObject(objIn)
                }
            }
        }
        return null
    }
    private fun byteArrayToBitmap(imageAsBytes: ByteArray): Bitmap? {
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size, opts)
    }

    private fun writeObject(objOut: ObjectOutputStream, bitmap: Bitmap){
        val byteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
        objOut.write(byteStream.toByteArray(), 0, byteStream.size())
    }
    private fun readObject(objIn: ObjectInputStream): Bitmap?{
        val byteStream = ByteArrayOutputStream()
        var b: Int
        while (objIn.read().also { b = it } != -1)
            byteStream.write(b)
        val bitmap = byteArrayToBitmap(byteStream.toByteArray())
        if(bitmap != null){
            Log.i("DiskCacheInfo", "${bitmap.width} ${bitmap.height} ${bitmap.byteCount}")
        } else {
            Log.i("DiskCacheInfo", "bitmap is null")
        }
        return bitmap
    }

    private fun md5Hash(str: String): String {
        try {
            // Create MD5 Hash
            val digest = MessageDigest.getInstance("MD5")
            digest.update(str.toByteArray())
            val messageDigest = digest.digest()
            // Create Hex String
            val hexString = StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

}