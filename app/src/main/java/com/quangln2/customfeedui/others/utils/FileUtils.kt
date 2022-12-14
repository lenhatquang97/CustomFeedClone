package com.quangln2.customfeedui.others.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.quangln2.customfeedui.R
import java.io.File
import java.io.FileOutputStream
import java.util.*


object FileUtils {
    fun convertContentUriToFileUri(contentURI: Uri, context: Context): String {
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
        return Uri.fromFile(result?.let { File(it) }).toString()
    }

    fun convertUnixTimestampToTime(unixTimestamp: String): String {
        val date = if (unixTimestamp.isEmpty()) Date() else Date(unixTimestamp.toLong())
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT+7")
        return sdf.format(date)
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
                    val bitmap = getBitmapItem(uri, context)
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

    private fun getBitmapItem(uri: Uri, context: Context): Bitmap{
        return when{
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                uri
            )
            else -> {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        }
    }
}