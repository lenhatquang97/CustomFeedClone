package com.quangln2.customfeedui.threadpool

import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


object TaskExecutor{
    val writingFiles = mutableSetOf<String>()
    private val numberOfCores = Runtime.getRuntime().availableProcessors()
    private var forBackgroundTasks: ThreadPoolExecutor? = null
    private var mainThreadExecutor: Executor? = null
    init {
        val threadPriorityBackground = PriorityThreadFactory(android.os.Process.THREAD_PRIORITY_BACKGROUND)

        forBackgroundTasks = ThreadPoolExecutor(
            numberOfCores * 2,
            numberOfCores * 2,
            60L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            threadPriorityBackground)

        mainThreadExecutor = MainThreadExecutor()
    }

    fun forBackgroundTasks(): ThreadPoolExecutor? {
        return forBackgroundTasks
    }
    fun forMainThreadTasks(): Executor? {
        return mainThreadExecutor
    }

}
