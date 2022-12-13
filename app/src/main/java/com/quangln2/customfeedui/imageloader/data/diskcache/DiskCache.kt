package com.quangln2.customfeedui.imageloader.data.diskcache
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.apache.commons.codec.binary.Hex
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/*
* Main purpose of managedTmpMap is to check whether file is valid. If file not valid (means different MD5 codes), updates again
* */

object DiskCache {
    //This is a boolean used to open and close experimental part to avoid error prone.
    const val isExperimental = false

    fun containsWith(key: String, context: Context): Boolean{
        val md5Key = md5Hash(key)
        return File(context.cacheDir, md5Key).exists()
    }

    //Key in DiskCache is MD5(webUrlOrFileUri)
    fun writeBitmapToDiskCache(key: String, bitmap: Bitmap, context: Context){
        val md5Key = md5Hash(key)
        val cacheFile = File(context.cacheDir, md5Key)
        if(!cacheFile.exists()){
            val anotherCacheFile = File(context.cacheDir, md5Key)
            val objOut = ObjectOutputStream(anotherCacheFile.outputStream())
            writeObject(objOut, bitmap, key)
            objOut.close()
        }
    }

    fun getBitmapFromDiskCache(key: String, context: Context): Bitmap?{
        val md5Key = md5Hash(key)
        val cacheFile = File(context.cacheDir, md5Key)
        if(cacheFile.exists()){
            val anotherCacheFile = File(context.cacheDir, md5Key)
            val objIn = ObjectInputStream(anotherCacheFile.inputStream())
            return readObject(objIn)
        }
        return null
    }
    private fun byteArrayToBitmap(imageAsBytes: ByteArray): Bitmap? {
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size, opts)
    }

    private fun writeObject(objOut: ObjectOutputStream, bitmap: Bitmap, key: String){
        val size = bitmap.byteCount
        val byteBuffer = ByteBuffer.allocate(size)
        bitmap.copyPixelsToBuffer(byteBuffer)
        bitmap.recycle()
        val hexString = Hex.encodeHexString(byteBuffer.array())
        objOut.writeChars(hexString)
        Log.i("DiskCacheInfo","Bitmap is null not $hexString")
    }
    private fun readObject(objIn: ObjectInputStream): Bitmap?{
        val hexString = objIn.readLine()
        val byteArray = Hex.decodeHex(hexString)
        val bitmap = byteArrayToBitmap(byteArray)
        if(bitmap != null){
            Log.i("DiskCacheInfo", "${bitmap.width} ${bitmap.height} ${bitmap.byteCount}")
        } else {
            Log.i("DiskCacheInfo", "bitmap is null $hexString")
        }
        objIn.close()
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