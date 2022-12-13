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
                val bitmap = BitmapUtils().decodeBitmapFromInputStream(uri.toString(), inputStream, width, height, context)
                if(DiskCache.isExperimental){
                    DiskCache.writeBitmapToDiskCache(uri.toString(), bitmap, context)
                }
                withContext(Dispatchers.Main){
                    if(!bitmap.isRecycled){
                        imageView.addToManagedAddress(uri.toString())
                        imageView.setImageBitmap(bitmap)
                    }

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
                        val managedBitmap = ManagedBitmap(bitmap, bitmap.width, bitmap.height)
                        LruBitmapCache.putIntoLruCache(uri.toString(), managedBitmap)
                        if(DiskCache.isExperimental){
                            DiskCache.writeBitmapToDiskCache(uri.toString(), bitmap, context)
                        }
                    }
                }
                withContext(Dispatchers.Main){
                    if(oldBmp != null && !oldBmp.getBitmap().isRecycled){
                        imageView.addToManagedAddress(uri.toString())
                        imageView.setImageBitmap(oldBmp.getBitmap())
                    }
                }
            }
        }
    }

    private fun loadEmptyImage(imageView: ImageView){
        scope.launch(Dispatchers.IO) {
            val bitmap = BitmapUtils().emptyBitmap(context)
            withContext(Dispatchers.Main){
                if(!bitmap.isRecycled){
                    imageView.addToManagedAddress("emptyBmp")
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun downloadImageAndThenLoadImageWithUrl(url: String, imageView: ImageView){
        val httpFetcher = HttpFetcher(url)
        scope.launch(Dispatchers.IO) {
            val onDone = fun(){
                val fileName = URLUtil.guessFileName(url, null, null)
                val file = File(context.filesDir, fileName)
                if(file.exists()){
                    loadImageWithUri(file.toUri(), imageView)
                }
            }
            httpFetcher.downloadImage(context, onDone)


        }
    }

    private fun isInMemoryCache(fileName: String): Boolean{
        val memoryCacheFile = File(context.cacheDir, fileName)
        return LruBitmapCache.containsKey(memoryCacheFile.toUri().toString())
    }
    private fun isInDiskCache(context: Context, fileName: String): Boolean{
        return DiskCache.containsWith(fileName, context)
    }
    private fun doesFileExist(fileName: String): Boolean{
        val file = File(context.cacheDir, fileName)
        return file.exists()
    }

    private fun handleMemoryCache(fileName: String, imageView: ImageView){
        val memoryCacheFile = File(context.cacheDir, fileName)
        loadImageWithUri(memoryCacheFile.toUri(), imageView)
    }

    private fun handleDiskCache(fileName: String, imageView: ImageView){
        val convertToUri = File(context.cacheDir, fileName)
        scope.launch(Dispatchers.IO) {
            val bitmap = DiskCache.getBitmapFromDiskCache(convertToUri.toUri().toString(), context)
            if(bitmap != null){
                LruBitmapCache.putIntoLruCache(convertToUri.toUri().toString(), ManagedBitmap(bitmap, bitmap.width, bitmap.height))
            }
            withContext(Dispatchers.Main) {
                imageView.setImageBitmap(bitmap)
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
            val convertToUri = File(context.cacheDir, fileName)

            if(isInMemoryCache(fileName)) {
                handleMemoryCache(fileName, imageView)
            }
            else if(isInDiskCache(context, convertToUri.toUri().toString()) && DiskCache.isExperimental) {
                handleDiskCache(fileName, imageView)
            }
            else if(doesFileExist(fileName)) {
                handleMemoryCache(fileName, imageView)
            }
            else {
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