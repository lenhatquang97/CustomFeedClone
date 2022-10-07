package com.quangln2.customfeed.others.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*


object FileUtils {
    fun getRealPathFromURI(contentURI: Uri, context: Context): String? {
        val result: String?
        val cursor: Cursor? = context.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx: Int =
                cursor.getColumnIndex(if (contentURI.path?.contains("mp4") == true) MediaStore.Video.VideoColumns.DATA else MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    fun convertUnixTimestampToTime(unixTimestamp: String): String {
        val date = if (unixTimestamp.isEmpty()) Date() else java.util.Date(unixTimestamp.toLong())
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm")
        sdf.timeZone = java.util.TimeZone.getTimeZone("GMT+7")
        return sdf.format(date)
    }

    fun getVideoThumbnail(uri: Uri, context: Context, url: String = ""): Drawable {
        val retriever = MediaMetadataRetriever()
        if (url.isNotEmpty()) {
            retriever.setDataSource(url, HashMap<String, String>())
        } else {
            retriever.setDataSource(context, uri)
        }
        val bitmap = retriever.getFrameAtTime(100)
        retriever.release()

        return BitmapDrawable(context.resources, bitmap)
    }
    fun getPermissionForStorage(context: Context, activity: Activity){
        val permissionCheck = ContextCompat.checkSelfPermission(context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }
}