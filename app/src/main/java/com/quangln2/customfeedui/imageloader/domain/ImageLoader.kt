package com.quangln2.customfeedui.imageloader.domain

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.net.toUri
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.imageloader.data.bitmap.ManagedBitmap
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import com.quangln2.customfeedui.imageloader.data.network.NetworkHelper
import com.quangln2.customfeedui.imageloader.data.source.ImageLocalDataSource
import com.quangln2.customfeedui.imageloader.data.source.ImageRemoteDataSource
import com.quangln2.customfeedui.imageloader.data.video.VideoFrame
import com.quangln2.customfeedui.others.utils.DownloadUtils
import com.quangln2.customfeedui.others.utils.FileUtils
import kotlinx.coroutines.*

class ImageLoader private constructor(
    private var context: Context,
    private var width: Int,
    private var height: Int,
    private var scope: CoroutineScope,
    private var bmpParams: BitmapCustomParams
) {

    private fun retrieveFrameFromLocalVideo(uri: Uri, imageView: ImageView) {
        scope.launch(Dispatchers.IO) {
            val resultBmp = VideoFrame.bitmapFrameFromLocalVideo(uri, context, bmpParams)
            if(resultBmp != null){
                async(Dispatchers.Main){
                    imageView.setImageBitmap(resultBmp)
                    val managedBitmap = ManagedBitmap(resultBmp, resultBmp.width, resultBmp.height)
                    LruBitmapCache.putIntoLruCache(uri.toString(), managedBitmap)
                }
            }
        }
    }

    //Flow: memory -> disk (check file integrity) -> internet
    private fun loadImageRemotely(webUrl: String, imageView: ImageView){
        val webUri = Uri.parse(webUrl)
        val webUrlWithoutQueryParams = "${webUri.scheme}://${webUri.host}${webUri.path}"

        //Use in case for getting thumbnail image if video exists
        val imageThumbnailUrl = NetworkHelper.convertVideoUrlToImageUrl(webUrlWithoutQueryParams)

        val fileName = URLUtil.guessFileName(imageThumbnailUrl, null, null)
        val actualPath = if(bmpParams.folderName.isEmpty()) fileName else "${bmpParams.folderName}/$fileName"

        val doesExistInMemCacheOrDisk = ImageLoaderUtils.isInMemoryCache(actualPath, context) || ImageLoaderUtils.doesFileExist(actualPath, context)
        val notWriting = !NetworkHelper.writingFiles.contains(actualPath)

        val localDataSource = ImageLocalDataSource(context, bmpParams)
        val remoteDataSource = ImageRemoteDataSource(context, bmpParams)

        if (doesExistInMemCacheOrDisk && notWriting) {
            if(ImageLoaderUtils.preCheckWithChecksum(actualPath, webUri, context)){
                localDataSource.handleCache(actualPath, imageView, width, height)
            } else {
                ImageLoaderUtils.deleteIfExists(actualPath, context)
                remoteDataSource.downAndLoadImage(imageThumbnailUrl, imageView, width, height)
            }
        }
        else {
            ImageLoaderUtils.createFolder(bmpParams.folderName, context)
            remoteDataSource.downAndLoadImage(imageThumbnailUrl, imageView, width, height)
        }
    }

    /*
    1. Check whether uri is content URI or file URI, if content URI -> change to file URI
    2. If type of URI is video, get frame of the video
    3. Else load image
     */
    private fun loadImageLocally(uriString: String, imageView: ImageView){
        val localDataSource = ImageLocalDataSource(context, bmpParams)
        val fileUri = if(uriString.startsWith("content"))
            FileUtils.convertContentUriToFileUri(uriString.toUri(), context)
        else
            uriString

        fileUri.apply {
            val mimeType = DownloadUtils.getMimeType(this)
            if (mimeType?.contains("video") == true) {
                retrieveFrameFromLocalVideo(uriString.toUri(), imageView)
            } else {
                localDataSource.loadImageWithUri(this.toUri(), imageView, width, height)
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