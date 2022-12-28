package com.quangln2.customfeedui.uitracking.ui

import android.webkit.URLUtil
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache
import com.quangln2.customfeedui.uitracking.data.MemoryStats

object UiTracking {
    const val THREAD_DOWNLOADING_IMAGE = "ImageFetcherThread"
    const val LOAD_WITH_URI = "LoadImageWithUriThread"

    var howManyTasksLoadingImage = 0


    private val memStats = MemoryStats()
    private fun isNotSpecialTask(name: String): Boolean {
        return !name.contains("Daemon") && !name.contains("Catcher")
    }

    fun getGeneralInfo(): String{
        val threadSet = Thread.getAllStackTraces().keys
        val waitingThreads = threadSet.filter {it.state == Thread.State.WAITING && isNotSpecialTask(it.name)}
        val numOfThreadFetchingImage = threadSet.filter {it.name == THREAD_DOWNLOADING_IMAGE}.size
        val numOfThreadLoadingImage = threadSet.filter {it.name == LOAD_WITH_URI}.size
        var waitingThreadName = StringBuilder()

        waitingThreads.forEach {
            waitingThreadName.append(String.format("%10s%n", it.name))
        }

        val usedHeapSizeFormat = "Used Heap Size: ${memStats.getUsedMemory()} MB\n"
        val numberOfWaitingThreadsFormat = "Number of waiting threads: ${waitingThreads.size}\n"
        val numOfThreadImage = "Number of threads downloading images: $numOfThreadFetchingImage\n"
        val numOfTaskWaitingDownloadingImage = "Number of tasks waiting for downloading images: ${BitmapTaskManager.executor.taskWaiting()}\n"

        val numOfLoadingBitmapURI = "Number of threads loading Bitmap with URI: $numOfThreadLoadingImage\n"
        val numOfTaskLoadingBitmapURI = "Number of tasks loading Bitmap with URI: $howManyTasksLoadingImage\n"

//        val allOfWaitingTasksDescription = "All of waiting tasks in waiting threads: $waitingThreadName\n"


        return usedHeapSizeFormat + numberOfWaitingThreadsFormat + numOfThreadImage + numOfTaskWaitingDownloadingImage + numOfLoadingBitmapURI + numOfTaskLoadingBitmapURI
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