package com.quangln2.customfeedui.utility

import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class UploadPostFile{
    companion object{
        //read file to byte array
        private fun readFileToByteArray(file: File): ByteArray{
            //read byte to byte
            val fileInputStream = file.inputStream()
            val byteArray = ByteArray(file.length().toInt())
            var read = 0
            var offset = 0
            while (read != -1 && offset < file.length()){
                read = fileInputStream.read(byteArray, offset, (file.length() - offset).toInt())
                offset += read
            }
            return byteArray
        }

        //TODO: Test upload file with data id
        fun uploadFileWithId(url: String, file: File, id: String): String{
            val connection = URL(url).openConnection() as HttpURLConnection
            val boundary = UUID.randomUUID().toString()
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
            connection.setRequestProperty("Connection", "Keep-Alive")
            try{
                connection.connect()
                val request = DataOutputStream(connection.outputStream)
                request.writeBytes("--$boundary\r\n")
                request.writeBytes("Content-Disposition: form-data; name=\"id\"\r\n\r\n")
                request.writeBytes(id + "\r\n")
                request.writeBytes("--$boundary\r\n")
                request.writeBytes("Content-Disposition: form-data; name=\"description\"\r\n\r\n")
                request.writeBytes(file.name + "\r\n")
                request.writeBytes("--$boundary\r\n")
                request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"\r\n\r\n")
                request.write(readFileToByteArray(file))
                request.writeBytes("\r\n")



                request.writeBytes("--$boundary--\r\n")
                request.flush()
                val responseCode = connection.responseCode
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    val br = BufferedReader(InputStreamReader(connection.inputStream))
                    val result = br.readLine()
                    br.close()
                    connection.disconnect()
                    return result
                } else if(responseCode == 307){
                    connection.disconnect()
                    return uploadFileWithId(connection.url.toString(), file, id)
                }
                else {
                    Log.d("Failed", "Upload file failed $responseCode")
                }

            } catch (e: java.lang.Exception){
                e.printStackTrace()
            }
            return "error"


        }
    }

}