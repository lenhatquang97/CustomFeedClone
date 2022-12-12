package com.quangln2.customfeedui.imageloader.data.diskcache
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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
    fun containsWith(key: String, context: Context): Boolean{
        val md5Key = md5Hash(key)
        return File(context.cacheDir, md5Key).exists()
    }

    //Key in DiskCache is MD5(webUrlOrFileUri)
    fun writeBitmapToDiskCache(key: String, bitmap: Bitmap, context: Context){
        val md5Key = md5Hash(key)
        val cacheFile = File(context.cacheDir, md5Key)
        if(cacheFile.exists()){
            val newBitmapHash = hashBitmap(bitmap)
            val objIn = ObjectInputStream(cacheFile.inputStream())
            val oldBitmapHash = readHashBitmapFromFile(objIn)
            Log.d("DiskCache", "${newBitmapHash == oldBitmapHash} with $oldBitmapHash $newBitmapHash")
            if(newBitmapHash == oldBitmapHash){
                return
            } else {
                cacheFile.delete()
                val objOut = ObjectOutputStream(cacheFile.outputStream())
                writeObject(objOut, bitmap)
            }
            return
        } else {
            val objOut = ObjectOutputStream(cacheFile.outputStream())
            writeObject(objOut, bitmap)
        }

    }

    fun getBitmapFromDiskCache(key: String, context: Context): Bitmap?{
        val md5Key = md5Hash(key)
        val cacheFile = File(context.cacheDir, md5Key)
        if(cacheFile.exists()){
            val objIn = ObjectInputStream(cacheFile.inputStream())
            return readObject(objIn)
        }
        return null
    }

    private fun writeObject(objOut: ObjectOutputStream, bitmap: Bitmap){
        val size = bitmap.byteCount
        val byteBuffer = ByteBuffer.allocate(size)
        bitmap.copyPixelsToBuffer(byteBuffer)

        objOut.writeInt(size)
        objOut.writeLong(hashBitmap(bitmap))

        val imageInByte = byteBuffer.array()
        objOut.writeObject(imageInByte)
    }
    private fun readObject(objIn: ObjectInputStream): Bitmap?{
        val bufferLength: Int = objIn.readInt()
        val hash = objIn.readLong()
        val imageByteArray = objIn.readObject() as ByteArray
        val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, bufferLength)
        if(bitmap != null){
            Log.i("DiskCacheInfo", "${bitmap.width} ${bitmap.height} ${bitmap.byteCount}")
        } else {
            Log.i("DiskCacheInfo", "bitmap is null $bufferLength $hash")
        }
        return bitmap
    }

    private fun readHashBitmapFromFile(objIn: ObjectInputStream): Long {
        objIn.readInt()
        return objIn.readLong()
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
    private fun hashBitmap(bmp: Bitmap): Long{
        var hash = 31
        for(x in 0 until bmp.width){
            for(y in 0 until bmp.height){
                hash = bmp.getPixel(x, y) + 31
            }
        }
        return hash.toLong()
    }



}