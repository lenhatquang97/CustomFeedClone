package com.quangln2.customfeedui.uitracking.ui

import android.webkit.URLUtil
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.extensions.taskWaiting
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import com.quangln2.customfeedui.uitracking.data.MemoryStats

object UiTracking {
    const val THREAD_DOWNLOADING_IMAGE = "ImageFetcherThread"
    const val LOAD_WITH_URI = "LoadImageWithUriThread"
    private val memStats = MemoryStats()

    private fun getOverallInfo(): String {
        val usedHeapSizeFormat = "Used Heap Size: ${memStats.getUsedMemory()} MB\n"
        val memorySizeFormat = "Size in LruCache: ${LruBitmapCache.memoryCache.size()}\n"
        val totalImagesIncludingDuplicatesFormat = "Number of images/thumbnails including duplicates: ${MyPostRender.numberOfImagesAndThumbnails}\n"

        val bitmapSizeFormat = "Number of bitmaps in LruCache: ${LruBitmapCache.memoryCache.snapshot().size}\n\n"

        val formatString = StringBuilder().apply {
            append("General:\n")
            append(usedHeapSizeFormat)
            append(memorySizeFormat)
            append(totalImagesIncludingDuplicatesFormat)
            append(bitmapSizeFormat)
        }
        return formatString.toString()
    }

    private fun getDownloadImageStat(threadSet: MutableSet<Thread>): String {
        val totalThreadDownImage = 4
        val numOfThreadDownImageRunning = threadSet.filter {it.name.contains(THREAD_DOWNLOADING_IMAGE) && it.state == Thread.State.RUNNABLE}.size
        val numOfTaskDownImageWaiting = BitmapTaskManager.executorDownloadingImage.taskWaiting()

        val formatTotalThreadDownImage = "Max threads: $totalThreadDownImage\n"
        val formatNumOfThreadDownImageRunning = "State RUNNABLE: $numOfThreadDownImageRunning\n"
        val formatNumOfThreadDownImageWaiting = "State IN THE QUEUE: $numOfTaskDownImageWaiting\n\n"

        val formatString = StringBuilder().apply {
            //Download images
            append("Downloading Images\n")
            append(formatTotalThreadDownImage)
            append(formatNumOfThreadDownImageRunning)
            append(formatNumOfThreadDownImageWaiting)
        }
        return formatString.toString()
    }

    private fun getLoadImageStat(threadSet: MutableSet<Thread>): String{
        val numOfThreadLoadingImage = threadSet.filter {it.name == LOAD_WITH_URI}.size
        val runnableLoadingImage = threadSet.filter {it.name == LOAD_WITH_URI && it.state == Thread.State.RUNNABLE}.size
        val waitingLoadingImage = threadSet.filter {it.name == LOAD_WITH_URI && it.state == Thread.State.WAITING}.size

        val formatMaxThreadLoadURI = "Max threads: $numOfThreadLoadingImage\n"
        val formatNumOfThreadRunning = "State RUNNABLE: $runnableLoadingImage\n"
        val formatNumOfTaskWaiting = "State IN THE QUEUE: $waitingLoadingImage\n"

        val formatString = StringBuilder().apply {
            append("Load bitmap with URI\n")
            append(formatMaxThreadLoadURI)
            append(formatNumOfThreadRunning)
            append(formatNumOfTaskWaiting)
        }
        return formatString.toString()
    }

    fun getGeneralInfo(): String{
        val threadSet = Thread.getAllStackTraces().keys

        val formatString = StringBuilder().apply {
            append(getOverallInfo())
            append(getDownloadImageStat(threadSet))
            append(getLoadImageStat(threadSet))
        }
        return formatString.toString()
    }

    fun getAllImageReferences(keyList: List<String>): String{
        val stringBuilder = StringBuilder()
        for(key in keyList){
            val managedBitmap = LruBitmapCache.getLruCacheWithoutIncreaseCount(key)
            if(managedBitmap != null){
                val fileName = URLUtil.guessFileName(key, null, null)
                stringBuilder.appendLine("Key: $fileName, Reference count: ${managedBitmap.referenceCount}")
            }
        }
        return stringBuilder.toString()
    }
}