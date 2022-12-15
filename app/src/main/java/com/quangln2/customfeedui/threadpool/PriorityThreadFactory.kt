package com.quangln2.customfeedui.threadpool

import java.util.concurrent.ThreadFactory
import kotlin.properties.Delegates

class PriorityThreadFactory: ThreadFactory {
    private var threadPriority by Delegates.notNull<Int>()
    constructor(threadPriority: Int){
        this.threadPriority = threadPriority
    }
    override fun newThread(r: Runnable?): Thread {
        val runnable = Runnable {
            try {
                android.os.Process.setThreadPriority(threadPriority)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            r?.run()
        }
        return Thread(runnable)
    }
}