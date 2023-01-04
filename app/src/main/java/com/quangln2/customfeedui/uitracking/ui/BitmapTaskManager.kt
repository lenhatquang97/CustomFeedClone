package com.quangln2.customfeedui.uitracking.ui

import android.os.Process
import java.util.concurrent.*

fun ExecutorService.taskWaiting(): Int{
    val threadPoolExecutor = this as ThreadPoolExecutor
    return threadPoolExecutor.queue.size
}

object BitmapTaskManager {
    var backgroundPriorityThreadFactory: ThreadFactory = PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND)
    val executorDownloadingImage: ExecutorService = ThreadPoolExecutor(
        4,
        4,
        30L,
        TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        backgroundPriorityThreadFactory
    )
}