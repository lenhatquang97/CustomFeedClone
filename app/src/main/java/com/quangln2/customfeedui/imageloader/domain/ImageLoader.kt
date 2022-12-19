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
import com.quangln2.customfeedui.imageloader.data.diskcache.DiskCache
import com.quangln2.customfeedui.imageloader.data.extension.addToManagedAddress
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import com.quangln2.customfeedui.imageloader.data.network.HttpFetcher
import com.quangln2.customfeedui.imageloader.data.network.NetworkHelper
import com.quangln2.customfeedui.others.utils.DownloadUtils
import com.quangln2.customfeedui.threadpool.TaskExecutor
import kotlinx.coroutines.*
import java.io.File

class ImageLoader(
    private var context: Context,
    private var width: Int,
    private var height: Int,
    private var scope: CoroutineScope
) {
    private fun loadImageWithUri(uri: Uri, imageView: ImageView, bmpParams: BitmapCustomParams) {
        scope.launch(Dispatchers.Default) {
            val httpFetcher = HttpFetcher(uri)
            val inputStream = httpFetcher.fetchImageByInputStream(context)
            if (inputStream != null) {
                val bitmap = BitmapUtils.decodeBitmapFromInputStream(uri.toString(), inputStream, width, height, bmpParams)
//                async(Dispatchers.IO){
//                    val actualKey = if(bmpParams.isFullScreen) "${uri}_fullScreen" else uri.toString()
//                    DiskCache.writeBitmapToDiskCache(actualKey, bitmap, context)
//                }
                withContext(Dispatchers.Main) {
                    if (!bitmap.isRecycled) {
                        async(Dispatchers.Main){
                            imageView.addToManagedAddress(uri.toString())
                            imageView.setImageBitmap(bitmap)
                        }
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
//                        async(Dispatchers.IO){
//                            DiskCache.writeBitmapToDiskCache(uri.toString(), bitmap, context)
//                        }
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

    private fun loadEmptyImage(imageView: ImageView) {
        scope.launch(Dispatchers.IO) {
            val bitmap = BitmapUtils.emptyBitmap()
            withContext(Dispatchers.Main) {
                if (!bitmap.isRecycled) {
                    async {
                        imageView.addToManagedAddress("emptyBmp")
                        imageView.setImageBitmap(bitmap)
                    }

                }
            }
        }
    }

    private fun downloadImageAndThenLoadImageWithUrl(url: String, imageView: ImageView, bmpParams: BitmapCustomParams) {
        val httpFetcher = HttpFetcher(url)
        val loadImage = fun(a: Uri, b: ImageView, c: BitmapCustomParams){
            loadImageWithUri(a, b, c)
        }
        httpFetcher.downloadImage(context, imageView, loadImage, bmpParams)

    }

    private fun isInMemoryCache(fileName: String): Boolean {
        val memoryCacheFile = File(context.cacheDir, fileName)
        return LruBitmapCache.containsKey(memoryCacheFile.toUri().toString())
    }

    private fun isInDiskCache(context: Context, fileName: String): Boolean {
        return DiskCache.containsWith(fileName, context)
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

    private fun handleDiskCache(filePath: String, imageView: ImageView) {
        val convertToUri = File(context.cacheDir, filePath)
        scope.launch(Dispatchers.IO) {
            val bitmap = DiskCache.getBitmapFromDiskCache(convertToUri.toUri().toString(), context)
            if (bitmap != null) {
                LruBitmapCache.putIntoLruCache(convertToUri.toUri().toString(), ManagedBitmap(bitmap, bitmap.width, bitmap.height))
            }
            withContext(Dispatchers.Main) {
                async {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }


    fun loadImage(webUrlOfFileUri: String, imageView: ImageView, bmpParams: BitmapCustomParams) {
        loadEmptyImage(imageView)
        if (webUrlOfFileUri.isEmpty()) {
            return
        } else if (URLUtil.isHttpUrl(webUrlOfFileUri) || URLUtil.isHttpsUrl(webUrlOfFileUri)) {
            if(bmpParams.folderName.isNotEmpty()){
                val folderCreation = File(context.cacheDir, bmpParams.folderName)
                if(!folderCreation.exists())
                    folderCreation.mkdir()
            }

            val imageThumbnailUrl = NetworkHelper.convertVideoUrlToImageUrl(webUrlOfFileUri)
            val fileName = URLUtil.guessFileName(imageThumbnailUrl, null, null)
            val actualPath = if(bmpParams.folderName.isEmpty()) fileName else "${bmpParams.folderName}/$fileName"
            if ((isInMemoryCache(actualPath) || doesFileExist(actualPath)) && !TaskExecutor.writingFiles.contains(actualPath)) {
                handleMemoryCache(actualPath, imageView, bmpParams)
            }
            else {
                downloadImageAndThenLoadImageWithUrl(imageThumbnailUrl, imageView, bmpParams)
            }
        } else {
            val mimeType = DownloadUtils.getMimeType(webUrlOfFileUri)
            if (mimeType?.contains("image") == true) {
                loadImageWithUri(webUrlOfFileUri.toUri(), imageView, bmpParams)
            } else {
                retrieveImageFromLocalVideo(webUrlOfFileUri.toUri(), imageView, bmpParams)
            }
        }
    }
}