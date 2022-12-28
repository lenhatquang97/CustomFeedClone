package com.quangln2.customfeedui.uitracking.ui

import android.webkit.URLUtil
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import com.quangln2.customfeedui.uitracking.data.MemoryStats

object UiTracking {
    const val THREAD_DOWNLOADING_IMAGE = "ImageFetcherThread"
    const val LOAD_WITH_URI = "LoadImageWithUriThread"

    private val memStats = MemoryStats()
    private fun isNotSpecialTask(name: String): Boolean {
        return !name.contains("Daemon") && !name.contains("Catcher")
    }

    private fun getOverallInfo(threadSet: MutableSet<Thread>): String {
        val waitingThreads = threadSet.filter {it.state == Thread.State.WAITING && isNotSpecialTask(it.name)}
        val usedHeapSizeFormat = "Used Heap Size: ${memStats.getUsedMemory()} MB\n"
        val numberOfWaitingThreadsFormat = "Number of waiting threads: ${waitingThreads.size}\n"
        val formatString = StringBuilder().apply {
            append(usedHeapSizeFormat)
            append(numberOfWaitingThreadsFormat)
        }
        return formatString.toString()
    }

    private fun getDownloadImageStat(threadSet: MutableSet<Thread>): String {
        val totalThreadDownImage = threadSet.filter { it.name.contains(THREAD_DOWNLOADING_IMAGE) }.size
        val numOfThreadDownImageRunning = threadSet.filter {it.name.contains(THREAD_DOWNLOADING_IMAGE) && it.state == Thread.State.RUNNABLE}.size
        val numOfTaskDownImageWaiting = BitmapTaskManager.executorDownloadingImage.taskWaiting()

        val formatTotalThreadDownImage = "Total number of threads downloading images: $totalThreadDownImage\n"
        val formatNumOfThreadDownImageRunning = "Number of threads running downloading images: $numOfThreadDownImageRunning\n"
        val formatNumOfTaskDownImageWaiting = "Number of tasks waiting for downloading images: $numOfTaskDownImageWaiting\n"

        val formatString = StringBuilder().apply {
            //Download images
            append(formatTotalThreadDownImage)
            append(formatNumOfThreadDownImageRunning)
            append(formatNumOfTaskDownImageWaiting)
        }
        return formatString.toString()
    }

    fun getGeneralInfo(): String{
        val threadSet = Thread.getAllStackTraces().keys
        val numOfThreadLoadingImage = threadSet.filter {it.name == LOAD_WITH_URI}.size
        val numOfLoadingBitmapURI = "Number of threads loading Bitmap with URI: $numOfThreadLoadingImage\n"
        val formatString = StringBuilder().apply {
            append(getOverallInfo(threadSet))
            append(getDownloadImageStat(threadSet))
            append(numOfLoadingBitmapURI)
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