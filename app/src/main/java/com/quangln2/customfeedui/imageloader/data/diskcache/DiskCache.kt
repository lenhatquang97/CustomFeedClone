package com.quangln2.customfeedui.imageloader.data.diskcache
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            CoroutineScope(Dispatchers.IO).launch {
                val newBitmapHash = hashBitmap(bitmap)
                val anotherCacheFile = File(context.cacheDir, md5Key)
                val objIn = ObjectInputStream(anotherCacheFile.inputStream())
                val oldBitmapHash = readHashBitmapFromFile(objIn)
                Log.d("DiskCache", "${newBitmapHash == oldBitmapHash} with $oldBitmapHash $newBitmapHash")
                if(newBitmapHash == oldBitmapHash || LruBitmapCache.containsKey(key)){
                    return@launch
                } else {
                    cacheFile.delete()
                    val anotherCacheFile = File(context.cacheDir, md5Key)
                    val objOut = ObjectOutputStream(anotherCacheFile.outputStream())
                    writeObject(objOut, bitmap, key)
                    objOut.close()
                }
            }
        } else {
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
    private fun base64ToBitmap(b64: String): Bitmap? {
        val imageAsBytes = Base64.decode(b64.toByteArray(), Base64.DEFAULT)
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size, opts)
    }

    private fun writeObject(objOut: ObjectOutputStream, bitmap: Bitmap, key: String){
        val size = bitmap.byteCount
        val byteBuffer = ByteBuffer.allocate(size)
        bitmap.copyPixelsToBuffer(byteBuffer)

        objOut.writeInt(size)
        objOut.writeLong(hashBitmap(bitmap))

        val b64Encode = Base64.encodeToString(byteBuffer.array(), Base64.DEFAULT)
        val hexString = base64ToHex(b64Encode)
        objOut.writeChars(hexString)
        Log.i("DiskCacheInfo","Bitmap is null not $hexString")
    }
    private fun readObject(objIn: ObjectInputStream): Bitmap?{
        val bufferLength: Int = objIn.readInt()
        val hash = objIn.readLong()
        val hexString = objIn.readLine()
        val b64Decode = hexToBase64(hexString)
        val bitmap = base64ToBitmap(b64Decode)
        if(bitmap != null){
            Log.i("DiskCacheInfo", "${bitmap.width} ${bitmap.height} ${bitmap.byteCount}")
        } else {
            Log.i("DiskCacheInfo", "bitmap is null $hexString $bufferLength $hash")
        }
        objIn.close()
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

    //Convert base64 string to hex
    private fun base64ToHex(base64: String): String {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }
    //Convert hex string to base64
    private fun hexToBase64(hex: String): String {
        val bytes = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}