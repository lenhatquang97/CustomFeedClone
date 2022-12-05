package com.quangln2.customfeedui.imageloader.domain

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.net.toUri
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapUtils
import com.quangln2.customfeedui.imageloader.data.network.HttpFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageLoader(
    private var context: Context,
    private var width: Int,
    private var height: Int,
    private var scope: CoroutineScope
    ) {
    private fun loadImageWithUrl(url: String, imageView: ImageView){
        scope.launch(Dispatchers.IO) {
            val inputStream = HttpFetcher.fetchImageByInputStream(url)
            if(inputStream != null){
                val bitmap = BitmapUtils().decodeBitmapFromInputStream(inputStream, width, height)
                withContext(Dispatchers.Main){
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun loadImageWithUri(uri: Uri, imageView: ImageView, context: Context){
        scope.launch(Dispatchers.IO) {
            val inputStream = HttpFetcher.fetchImageByInputStream(uri, context)
            if(inputStream != null){
                val bitmap = BitmapUtils().decodeBitmapFromInputStream(inputStream, width, height)
                withContext(Dispatchers.Main){
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun loadEmptyImage(imageView: ImageView){
        scope.launch(Dispatchers.IO) {
            val bitmap = BitmapUtils().emptyBitmap()
            withContext(Dispatchers.Main){
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    fun loadImage(uri: String, imageView: ImageView){
        //TODO: Implement this method
        /**
         * Step 1: Check if uri is webUrl or fileUri
         * If fileUri, load image with uri
         * If webUrl, do:
         * Step 2: Check if available images
         * If available, load image in disk
         * If not, do:
         * Step 3: Download image
         * Step 4: Save image to disk
         * Step 5: Load image from disk
         */
        if(uri.isEmpty()){
            loadEmptyImage(imageView)
        }
        else if(URLUtil.isHttpUrl(uri) || URLUtil.isHttpsUrl(uri)){
            loadImageWithUrl(uri, imageView)
        } else {
            loadImageWithUri(uri.toUri(), imageView, context)
        }
    }
}