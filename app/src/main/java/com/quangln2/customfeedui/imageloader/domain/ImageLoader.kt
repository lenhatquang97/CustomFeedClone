package com.quangln2.customfeedui.imageloader.domain

import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.net.toUri
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapUtils
import com.quangln2.customfeedui.imageloader.data.bitmap.ManagedBitmap
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import com.quangln2.customfeedui.imageloader.data.network.CodeUtils
import com.quangln2.customfeedui.imageloader.data.network.HttpFetcher
import com.quangln2.customfeedui.others.utils.DownloadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImageLoader(
    private var context: Context,
    private var width: Int,
    private var height: Int,
    private var scope: CoroutineScope
    ) {
    private fun loadImageWithUri(uri: Uri, imageView: ImageView){
        scope.launch(Dispatchers.IO) {
            val httpFetcher = HttpFetcher(uri)
            val inputStream = httpFetcher.fetchImageByInputStream(context)
            if(inputStream != null){
                val bitmap = BitmapUtils().decodeBitmapFromInputStream(uri.toString(), inputStream, width, height)
                withContext(Dispatchers.Main){
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun retrieveImageFromLocalVideo(uri: Uri, imageView: ImageView){
        scope.launch(Dispatchers.IO) {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
            cursor?.apply {
                moveToFirst()
                val columnIndex = getColumnIndex(filePathColumn[0])
                val picturePath = getString(columnIndex)
                close()
                val oldBmp = LruBitmapCache.getLruCache(uri.toString())
                if(oldBmp == null){
                    val bitmap = ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
                    if(bitmap != null && !bitmap.isRecycled){
                        LruBitmapCache.putIntoLruCache(uri.toString(), ManagedBitmap(bitmap, bitmap.width, bitmap.height))
                    }
                }
                withContext(Dispatchers.Main){
                    val bmpLru = LruBitmapCache.getLruCache(uri.toString())
                    if(bmpLru != null && !bmpLru.getBitmap().isRecycled){
                        imageView.setImageBitmap(bmpLru.getBitmap())
                    }

                }
            }
        }
    }

    private fun loadEmptyImage(imageView: ImageView){
        scope.launch(Dispatchers.IO) {
            val bitmap = BitmapUtils().emptyBitmap()
            withContext(Dispatchers.Main){
                if(!bitmap.isRecycled){
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun downloadImageAndThenLoadImageWithUrl(url: String, imageView: ImageView){
        val httpFetcher = HttpFetcher(url)
        scope.launch(Dispatchers.IO) {
            httpFetcher.downloadImage(context)
            val fileName = URLUtil.guessFileName(url, null, null)
            val file = File(context.filesDir, fileName)
            if(file.exists()){
                loadImageWithUri(file.toUri(), imageView)
            }
        }
    }

    fun loadImage(webUrlOfFileUri: String, imageView: ImageView){
        loadEmptyImage(imageView)
        if(webUrlOfFileUri.isEmpty()){
            return
        }
        else if(URLUtil.isHttpUrl(webUrlOfFileUri) || URLUtil.isHttpsUrl(webUrlOfFileUri)){
            val imageThumbnailUrl = CodeUtils.convertVideoUrlToImageUrl(webUrlOfFileUri)
            val fileName = URLUtil.guessFileName(imageThumbnailUrl, null, null)
            val cacheFile = File(context.cacheDir, fileName)
            if(cacheFile.exists()){
                loadImageWithUri(cacheFile.toUri(), imageView)
            }
            else{
                downloadImageAndThenLoadImageWithUrl(imageThumbnailUrl, imageView)
            }
        }
        else{
            val mimeType = DownloadUtils.getMimeType(webUrlOfFileUri)
            if(mimeType?.contains("image") == true){
                loadImageWithUri(webUrlOfFileUri.toUri(), imageView)
            }
            else{
                retrieveImageFromLocalVideo(webUrlOfFileUri.toUri(), imageView)
            }
        }
    }
}