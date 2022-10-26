package com.quangln2.customfeed.others.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.quangln2.customfeed.R
import java.io.File
import java.io.FileOutputStream
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
        val date = if (unixTimestamp.isEmpty()) Date() else Date(unixTimestamp.toLong())
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm")
        sdf.timeZone = TimeZone.getTimeZone("GMT+7")
        return sdf.format(date)
    }

    fun getPermissionForStorage(context: Context, activity: Activity): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            return false
        }

        return true
    }

    fun getPermissionForCall(context: Context, activity: Activity): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_PHONE_STATE
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.READ_PHONE_STATE), 1)
            return false
        }

        return true
    }

    fun getPermissionForStorageWithMultipleTimesDenial(context: Context): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                context,
                context.resources.getString(R.string.guide_to_get_storage_permission),
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", context.packageName, null)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.data = uri
            context.startActivity(intent)
            return false
        }

        return true
    }

    fun compressImagesAndVideos(uriLists: MutableList<Uri>, context: Context): MutableList<Uri> {
        val result: MutableList<Uri> = mutableListOf()
        for (uri in uriLists) {
            val mimeTypeForMultipart = context.contentResolver?.getType(uri)
            if (mimeTypeForMultipart != null) {
                if (mimeTypeForMultipart.startsWith("image/")) {
                    val file = File(context.filesDir, "${UUID.randomUUID()}.jpg")
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)
                    result.add(file.toUri())
                } else if (mimeTypeForMultipart.startsWith("video/")) {
                    result.add(uri)
                }
            }
        }

        return result
    }
}