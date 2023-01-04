package com.quangln2.customfeedui.imageloader.domain

import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.net.toUri
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapUtils
import com.quangln2.customfeedui.imageloader.data.bitmap.ManagedBitmap
import com.quangln2.customfeedui.imageloader.data.extension.addToManagedAddress
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import com.quangln2.customfeedui.imageloader.data.network.HttpFetcher
import com.quangln2.customfeedui.imageloader.data.network.NetworkHelper
import com.quangln2.customfeedui.others.utils.DownloadUtils
import com.quangln2.customfeedui.others.utils.FileUtils
import com.quangln2.customfeedui.uitracking.ui.UiTracking
import kotlinx.coroutines.*
import java.io.File

class ImageLoader(
    private var context: Context,
    private var width: Int,
    private var height: Int,
    private var scope: CoroutineScope
) {
    private fun loadImageWithUri(uri: Uri, imageView: ImageView, bmpParams: BitmapCustomParams) {
        val httpFetcher = HttpFetcher(uri)
        CoroutineScope(Dispatchers.Default).launch {
            Thread.currentThread().name = UiTracking.LOAD_WITH_URI + uri.toString()
            val inputStream = httpFetcher.fetchImageByInputStream(context)
            if (inputStream != null) {
                val bitmap = BitmapUtils.decodeBitmapFromInputStream(uri.toString(), inputStream, width, height, bmpParams)
                if (bitmap != null) {
                    withContext(Dispatchers.Main){
                        imageView.addToManagedAddress(uri.toString())
                        imageView.setImageBitmap(bitmap)
                    }

                }
            }
        }

    }

    private fun retrieveImageFromLocalVideo(uri: Uri, imageView: ImageView, bmpParams: BitmapCustomParams) {
        scope.launch(Dispatchers.IO) {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
            cursor?.apply {
                moveToFirst()
                val columnIndex = getColumnIndex(filePathColumn[0])
                val picturePath = getString(columnIndex)
                close()
                val oldBmp = LruBitmapCache.getLruCache(uri.toString(), bmpParams)
                if (oldBmp == null) {
                    val bitmap = ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
                    if (bitmap != null) {
                        async(Dispatchers.Main){
                            imageView.setImageBitmap(bitmap)
                            val managedBitmap = ManagedBitmap(bitmap, bitmap.width, bitmap.height)
                            LruBitmapCache.putIntoLruCache(uri.toString(), managedBitmap)
                        }
                    }
                } else {
                    async(Dispatchers.Main) {
                        imageView.addToManagedAddress(uri.toString())
                        imageView.setImageBitmap(oldBmp.getBitmap())
                    }
                }
            }
        }
    }

    private fun downloadImageAndThenLoadImageWithUrl(url: String, imageView: ImageView, bmpParams: BitmapCustomParams) {
        val httpFetcher = HttpFetcher(url)
        val loadImageCallback = fun(){
            imageView.post {
                val fileName = URLUtil.guessFileName(url, null, null)
                val actualPath = if(bmpParams.folderName.isEmpty()) fileName else "${bmpParams.folderName}/$fileName"
                val memoryCacheFile = File(context.cacheDir, actualPath)
                if (memoryCacheFile.exists()) {
                    loadImageWithUri(memoryCacheFile.toUri(), imageView, bmpParams)
                }
            }
        }
        httpFetcher.downloadImage(context, bmpParams, loadImageCallback)
    }

    private fun isInMemoryCache(fileName: String): Boolean {
        val memoryCacheFile = File(context.cacheDir, fileName)
        return LruBitmapCache.containsKey(memoryCacheFile.toUri().toString())
    }

    private fun doesFileExist(fileName: String): Boolean {
        val file = File(context.cacheDir, fileName)
        return file.exists()
    }

    private fun handleMemoryCache(filePath: String, imageView: ImageView, bmpParams: BitmapCustomParams) {
        if(bmpParams.folderName.isNotEmpty()){
            val folderCreation = File(context.cacheDir, bmpParams.folderName)
            if(!folderCreation.exists())
                folderCreation.mkdir()
        }
        val memoryCacheFile = File(context.cacheDir, filePath)
        if (memoryCacheFile.exists()) {
            loadImageWithUri(memoryCacheFile.toUri(), imageView, bmpParams)
        }

    }

    private fun loadImageLocally(webUrlOrFileUri: String, imageView: ImageView, bmpParams: BitmapCustomParams){
        val actualUri = if(webUrlOrFileUri.startsWith("content"))
            FileUtils.convertContentUriToFileUri(webUrlOrFileUri.toUri(), context)
        else
            webUrlOrFileUri
        actualUri?.apply {
            val mimeType = DownloadUtils.getMimeType(this)
            if (mimeType?.contains("video") == true) {
                retrieveImageFromLocalVideo(webUrlOrFileUri.toUri(), imageView, bmpParams)
            } else {
                loadImageWithUri(this.toUri(), imageView, bmpParams)
            }
        }
    }

    private fun loadImageRemotely(webUrlOrFileUri: String, imageView: ImageView, bmpParams: BitmapCustomParams, callback: () -> Unit = {}){
        if(bmpParams.folderName.isNotEmpty()){
            val folderCreation = File(context.cacheDir, bmpParams.folderName)
            if(!folderCreation.exists())
                folderCreation.mkdir()
        }

        val imageThumbnailUrl = NetworkHelper.convertVideoUrlToImageUrl(webUrlOrFileUri)
        val fileName = URLUtil.guessFileName(imageThumbnailUrl, null, null)
        val actualPath = if(bmpParams.folderName.isEmpty()) fileName else "${bmpParams.folderName}/$fileName"
        if ((isInMemoryCache(actualPath) || doesFileExist(actualPath)) && !NetworkHelper.writingFiles.contains(actualPath)) {
            handleMemoryCache(actualPath, imageView, bmpParams)
        }
        else {
            downloadImageAndThenLoadImageWithUrl(imageThumbnailUrl, imageView, bmpParams)

        }
    }


    fun loadImage(webUrlOrFileUri: String, imageView: ImageView, bmpParams: BitmapCustomParams) {
        if (webUrlOrFileUri.isEmpty()) {
            return
        } else if (URLUtil.isHttpUrl(webUrlOrFileUri) || URLUtil.isHttpsUrl(webUrlOrFileUri)) {
            loadImageRemotely(webUrlOrFileUri, imageView, bmpParams)
        } else {
            loadImageLocally(webUrlOrFileUri, imageView, bmpParams)
        }

    }
}