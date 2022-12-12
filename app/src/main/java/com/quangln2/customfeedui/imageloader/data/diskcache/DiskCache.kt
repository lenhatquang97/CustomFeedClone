package com.quangln2.customfeedui.imageloader.data.diskcache
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/*
* Main purpose of managedTmpMap is to check whether file is valid. If file not valid (means different MD5 codes), updates again
* */
class DiskCache : Closeable{
    private val MANAGED = "managed"
    private val MANAGED_TMP = "managed.tmp"
    private val fileManagedMap = mutableMapOf<String, String>()

    //Load all into maps first
    fun initManagedFile(context: Context){
        val managedFile = File(context.cacheDir, MANAGED_TMP)
        if(!managedFile.exists()){
           managedFile.createNewFile()
        } else {
            val fileInputStream = FileInputStream(managedFile)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var line = bufferedReader.readLine()
            while (line != null){
                //Handle logic


                line = bufferedReader.readLine()
            }
        }
    }

    //key is the same as in Bitmap, but value is MD5
    fun writeToManagedTmp(key: String, value: String){
        fileManagedMap[key] = value
    }


    fun readInManagedTmp(key: String): String? = fileManagedMap[key]

    //Key in DiskCache is MD5(webUrlOrFileUri)
    fun writeBitmapToDiskCache(key: String, bitmap: Bitmap, context: Context){
        val md5Key = md5Hash(key)
        val cacheFile = File(context.cacheDir, md5Key)
        if(cacheFile.exists()){
            //Calculate hash of new bitmap

            //Compare hash of new bitmap and disk bitmap


            //If yes, delete
            cacheFile.delete()
            val newFile = File(context.cacheDir, md5Key)
            val objOut = ObjectOutputStream(newFile.outputStream())
            writeObject(objOut, bitmap)
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

    //true if remove successfully, false if fail to remove
    fun removeBitmapFromDiskCache(key: String, context: Context): Boolean{
        val md5Key = md5Hash(key)
        val cacheFile = File(context.cacheDir, md5Key)
        if(cacheFile.exists()){
            return cacheFile.delete()
        }
        return true
    }

    override fun close() {

    }

    fun serializeBitmap(){

    }

    fun deserializeBitmap(){

    }

    private fun writeObject(objOut: ObjectOutputStream, bitmap: Bitmap){
        val byteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteStream)
        val bitmapBytes = byteStream.toByteArray()
        objOut.write(bitmapBytes, 0, bitmapBytes.size)
    }

    private fun readObject(objIn: ObjectInputStream): Bitmap?{
        val byteStream = ByteArrayOutputStream()
        var byteNum: Int
        while (objIn.read().also { byteNum = it } != -1){
            byteStream.write(byteNum)
        }
        val bitmapBytes = byteStream.toByteArray()
        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.size)
    }

    private fun md5Hash(str: String): String {
        try {
            // Create MD5 Hash
            val digest = MessageDigest.getInstance("MD5")
            digest.update(str.toByte())
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