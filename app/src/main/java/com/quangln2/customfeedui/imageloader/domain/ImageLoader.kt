package com.quangln2.customfeedui.imageloader.domain

import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.net.toUri
import com.quangln2.customfeedui.extensions.md5
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

    private fun downAndLoadImage(url: String, imageView: ImageView, bmpParams: BitmapCustomParams) {
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

    private fun handleCache(filePath: String, imageView: ImageView, bmpParams: BitmapCustomParams) {
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

    private fun preCheckWithChecksum(actualPath: String, webUri: Uri): Boolean{
        val checkSumParams = webUri.getQueryParameters("checksum")
        val checkSumValue = if (checkSumParams.isNotEmpty()) checkSumParams[0] else ""
        val fileContain = File(context.cacheDir, actualPath)
        if(fileContain.exists()){
            val actualChecksum = fileContain.md5()
            Log.d("ImageLoader", "Checksum on the server: $checkSumValue vs Checksum in reality: $actualChecksum")
            return checkSumValue == actualChecksum || checkSumValue.isEmpty()
        }
        return false
    }


    private fun loadImageRemotely(webUrl: String, imageView: ImageView, bmpParams: BitmapCustomParams){
        val webUri = Uri.parse(webUrl)
        val webUrlWithoutQueryParams = "${webUri.scheme}://${webUri.host}${webUri.path}"

        //Use in case for getting thumbnail image if video exists
        val imageThumbnailUrl = NetworkHelper.convertVideoUrlToImageUrl(webUrlWithoutQueryParams)

        val fileName = URLUtil.guessFileName(imageThumbnailUrl, null, null)
        val actualPath = if(bmpParams.folderName.isEmpty()) fileName else "${bmpParams.folderName}/$fileName"

        val doesExistInMemCacheOrDisk = ImageLoaderUtils.isInMemoryCache(actualPath, context) || ImageLoaderUtils.doesFileExist(actualPath, context)
        val notWriting = !NetworkHelper.writingFiles.contains(actualPath)

        if (doesExistInMemCacheOrDisk && notWriting) {
            if(preCheckWithChecksum(actualPath, webUri)){
                handleCache(actualPath, imageView, bmpParams)
            } else {
                ImageLoaderUtils.deleteIfExists(actualPath, context)
                downAndLoadImage(imageThumbnailUrl, imageView, bmpParams)
            }
        }
        else {
            ImageLoaderUtils.createFolder(bmpParams.folderName, context)
            downAndLoadImage(imageThumbnailUrl, imageView, bmpParams)
        }
    }

    private fun loadImageLocally(uriString: String, imageView: ImageView, bmpParams: BitmapCustomParams){
        val fileUri = if(uriString.startsWith("content"))
            FileUtils.convertContentUriToFileUri(uriString.toUri(), context)
        else
            uriString
        fileUri.apply {
            val mimeType = DownloadUtils.getMimeType(this)
            if (mimeType?.contains("video") == true) {
                retrieveImageFromLocalVideo(uriString.toUri(), imageView, bmpParams)
            } else {
                loadImageWithUri(this.toUri(), imageView, bmpParams)
            }
        }
    }

    fun loadImage(webUrlOrFileUri: String, imageView: ImageView, bmpParams: BitmapCustomParams) {
        val isWebUrl = URLUtil.isHttpUrl(webUrlOrFileUri) || URLUtil.isHttpsUrl(webUrlOrFileUri)
        if (webUrlOrFileUri.isEmpty()) return
        else if (isWebUrl) loadImageRemotely(webUrlOrFileUri, imageView, bmpParams)
        else loadImageLocally(webUrlOrFileUri, imageView, bmpParams)
    }
}