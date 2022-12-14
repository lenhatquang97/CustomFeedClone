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
import com.quangln2.customfeedui.imageloader.data.diskcache.DiskCache
import com.quangln2.customfeedui.imageloader.data.extension.addToManagedAddress
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import com.quangln2.customfeedui.imageloader.data.network.CodeUtils
import com.quangln2.customfeedui.imageloader.data.network.HttpFetcher
import com.quangln2.customfeedui.others.utils.DownloadUtils
import kotlinx.coroutines.*
import java.io.File

class ImageLoader(
    private var context: Context,
    private var width: Int,
    private var height: Int,
    private var scope: CoroutineScope
) {
    private fun loadImageWithUri(uri: Uri, imageView: ImageView, countRef: Boolean) {
        scope.launch(Dispatchers.IO) {
            val httpFetcher = HttpFetcher(uri)
            val inputStream = httpFetcher.fetchImageByInputStream(context)
            if (inputStream != null) {
                val bitmap = BitmapUtils().decodeBitmapFromInputStream(uri.toString(), inputStream, width, height, countRef)
                val managedBitmap = ManagedBitmap(bitmap, width, height)
                LruBitmapCache.putIntoLruCache(uri.toString(), managedBitmap)
                withContext(Dispatchers.Main) {
                    if (!bitmap.isRecycled) {
                        imageView.addToManagedAddress(uri.toString())
                        async {
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                }
                async {
                    DiskCache.writeBitmapToDiskCache(uri.toString(), bitmap, context)
                }
            }
        }
    }

    private fun retrieveImageFromLocalVideo(uri: Uri, imageView: ImageView, countRef: Boolean) {
        scope.launch(Dispatchers.IO) {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, filePathColumn, null, null, null)
            cursor?.apply {
                moveToFirst()
                val columnIndex = getColumnIndex(filePathColumn[0])
                val picturePath = getString(columnIndex)
                close()
                val oldBmp = LruBitmapCache.getLruCache(uri.toString(), countRef)
                if (oldBmp == null) {
                    val bitmap = ThumbnailUtils.createVideoThumbnail(picturePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
                    if (bitmap != null && !bitmap.isRecycled) {
                        val managedBitmap = ManagedBitmap(bitmap, bitmap.width, bitmap.height)
                        LruBitmapCache.putIntoLruCache(uri.toString(), managedBitmap)
                        async {
                            DiskCache.writeBitmapToDiskCache(uri.toString(), bitmap, context)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    if (oldBmp != null && !oldBmp.getBitmap().isRecycled) {
                        imageView.addToManagedAddress(uri.toString())
                        async {
                            imageView.setImageBitmap(oldBmp.getBitmap())
                        }
                    }
                }
            }
        }
    }

    private fun loadEmptyImage(imageView: ImageView) {
        scope.launch(Dispatchers.IO) {
            val bitmap = BitmapUtils().emptyBitmap()
            withContext(Dispatchers.Main) {
                if (!bitmap.isRecycled) {
                    imageView.addToManagedAddress("emptyBmp")
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun downloadImageAndThenLoadImageWithUrl(url: String, imageView: ImageView, countRef: Boolean) {
        val httpFetcher = HttpFetcher(url)
        val onDone = fun(fileName: String) {
            val file = File(context.cacheDir, fileName)
            if (file.exists()) {
                loadImageWithUri(file.toUri(), imageView, countRef)
            }
        }
        httpFetcher.downloadImage(context, onDone)
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

    private fun handleMemoryCache(fileName: String, imageView: ImageView, countRef: Boolean) {
        val memoryCacheFile = File(context.cacheDir, fileName)
        if (memoryCacheFile.exists()) {
            loadImageWithUri(memoryCacheFile.toUri(), imageView, countRef)
        }

    }

    private fun handleDiskCache(fileName: String, imageView: ImageView) {
        val convertToUri = File(context.cacheDir, fileName)
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


    fun loadImage(webUrlOfFileUri: String, imageView: ImageView, countRef: Boolean = true) {
        loadEmptyImage(imageView)
        if (webUrlOfFileUri.isEmpty()) {
            return
        } else if (URLUtil.isHttpUrl(webUrlOfFileUri) || URLUtil.isHttpsUrl(webUrlOfFileUri)) {
            val imageThumbnailUrl = CodeUtils.convertVideoUrlToImageUrl(webUrlOfFileUri)
            val fileName = URLUtil.guessFileName(imageThumbnailUrl, null, null)
            val convertToUri = File(context.cacheDir, fileName)
            if (doesFileExist(fileName) && !isInMemoryCache(fileName) || isInMemoryCache(fileName)) {
                handleMemoryCache(fileName, imageView, countRef)
            } else if (isInDiskCache(context, convertToUri.toUri().toString()) && DiskCache.isExperimental) {
                handleDiskCache(fileName, imageView)
            } else {
                downloadImageAndThenLoadImageWithUrl(imageThumbnailUrl, imageView, countRef)
            }
        } else {
            val mimeType = DownloadUtils.getMimeType(webUrlOfFileUri)
            if (mimeType?.contains("image") == true) {
                loadImageWithUri(webUrlOfFileUri.toUri(), imageView, countRef)
            } else {
                retrieveImageFromLocalVideo(webUrlOfFileUri.toUri(), imageView, countRef)
            }
        }
    }
}