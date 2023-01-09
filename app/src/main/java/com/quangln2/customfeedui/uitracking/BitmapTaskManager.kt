package com.quangln2.customfeedui.uitracking

import android.os.Process
import java.util.concurrent.*

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