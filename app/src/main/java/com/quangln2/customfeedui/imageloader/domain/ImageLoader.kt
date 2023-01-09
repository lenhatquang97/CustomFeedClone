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
import com.quangln2.customfeedui.uitracking.UiTracking
import kotlinx.coroutines.*
import java.io.File

class ImageLoader private constructor(
    private var context: Context,
    private var width: Int,
    private var height: Int,
    private var scope: CoroutineScope,
    private var bmpParams: BitmapCustomParams
) {
    private fun loadImageWithUri(uri: Uri, imageView: ImageView) {
        val httpFetcher = HttpFetcher(uri)
        scope.launch(Dispatchers.Default) {
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

    private fun retrieveImageFromLocalVideo(uri: Uri, imageView: ImageView) {
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

    private fun downAndLoadImage(url: String, imageView: ImageView) {
        val httpFetcher = HttpFetcher(url)
        val loadImageCallback = fun(){
            imageView.post {
                val fileName = URLUtil.guessFileName(url, null, null)
                val actualPath = if(bmpParams.folderName.isEmpty()) fileName else "${bmpParams.folderName}/$fileName"
                val memoryCacheFile = File(context.cacheDir, actualPath)
                if (memoryCacheFile.exists()) {
                    loadImageWithUri(memoryCacheFile.toUri(), imageView)
                }
            }
        }
        httpFetcher.downloadImage(context, bmpParams, loadImageCallback)
    }

    private fun handleCache(filePath: String, imageView: ImageView) {
        if(bmpParams.folderName.isNotEmpty()){
            val folderCreation = File(context.cacheDir, bmpParams.folderName)
            if(!folderCreation.exists())
                folderCreation.mkdir()
        }
        val memoryCacheFile = File(context.cacheDir, filePath)
        if (memoryCacheFile.exists()) {
            loadImageWithUri(memoryCacheFile.toUri(), imageView)
        }
    }

    private fun loadImageRemotely(webUrl: String, imageView: ImageView){
        val webUri = Uri.parse(webUrl)
        val webUrlWithoutQueryParams = "${webUri.scheme}://${webUri.host}${webUri.path}"

        //Use in case for getting thumbnail image if video exists
        val imageThumbnailUrl = NetworkHelper.convertVideoUrlToImageUrl(webUrlWithoutQueryParams)

        val fileName = URLUtil.guessFileName(imageThumbnailUrl, null, null)
        val actualPath = if(bmpParams.folderName.isEmpty()) fileName else "${bmpParams.folderName}/$fileName"

        val doesExistInMemCacheOrDisk = ImageLoaderUtils.isInMemoryCache(actualPath, context) || ImageLoaderUtils.doesFileExist(actualPath, context)
        val notWriting = !NetworkHelper.writingFiles.contains(actualPath)

        if (doesExistInMemCacheOrDisk && notWriting) {
            if(ImageLoaderUtils.preCheckWithChecksum(actualPath, webUri, context)){
                handleCache(actualPath, imageView)
            } else {
                ImageLoaderUtils.deleteIfExists(actualPath, context)
                downAndLoadImage(imageThumbnailUrl, imageView)
            }
        }
        else {
            ImageLoaderUtils.createFolder(bmpParams.folderName, context)
            downAndLoadImage(imageThumbnailUrl, imageView)
        }
    }

    private fun loadImageLocally(uriString: String, imageView: ImageView){
        val fileUri = if(uriString.startsWith("content"))
            FileUtils.convertContentUriToFileUri(uriString.toUri(), context)
        else
            uriString
        fileUri.apply {
            val mimeType = DownloadUtils.getMimeType(this)
            if (mimeType?.contains("video") == true) {
                retrieveImageFromLocalVideo(uriString.toUri(), imageView)
            } else {
                loadImageWithUri(this.toUri(), imageView)
            }
        }
    }

    fun loadImage(webUrlOrFileUri: String, imageView: ImageView) {
        val isWebUrl = URLUtil.isHttpUrl(webUrlOrFileUri) || URLUtil.isHttpsUrl(webUrlOrFileUri)
        if (webUrlOrFileUri.isEmpty()) return
        else if (isWebUrl) loadImageRemotely(webUrlOrFileUri, imageView)
        else loadImageLocally(webUrlOrFileUri, imageView)
    }

    data class Builder(
        var width: Int = 0,
        var height: Int = 0,
        var scope: CoroutineScope = CoroutineScope(Job()),
        var bmpParams: BitmapCustomParams = BitmapCustomParams()
    ) {
        fun resize(width: Int, height: Int) = apply {
            this.width = width
            this.height = height
        }
        fun inScope(scope: CoroutineScope) = apply{
            this.scope = scope
        }

        fun putIntoFolder(folderName: String) = apply {
            this.bmpParams.folderName = folderName
        }

        fun makeFullScreen() = apply {
            this.bmpParams.isFullScreen = true
        }

        fun build(context: Context) = ImageLoader(context, width, height, scope, bmpParams)
    }

}