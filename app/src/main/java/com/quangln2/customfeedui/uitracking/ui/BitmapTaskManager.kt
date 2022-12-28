package com.quangln2.customfeedui.uitracking.ui

import android.os.Process
import java.util.concurrent.*

fun ExecutorService.taskWaiting(): Int{
    val threadPoolExecutor = this as ThreadPoolExecutor
    return threadPoolExecutor.queue.size
}
val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()

object BitmapTaskManager {
    var backgroundPriorityThreadFactory: ThreadFactory = PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND)
    val executorDownloadingImage: ExecutorService = ThreadPoolExecutor(
        2 * NUMBER_OF_CORES,
        2 * NUMBER_OF_CORES,
        30L,
        TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        backgroundPriorityThreadFactory
    )

}