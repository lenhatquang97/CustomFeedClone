package com.quangln2.customfeedui.uitracking

import java.util.concurrent.ThreadFactory

class PriorityThreadFactory(private val mThreadPriority: Int) : ThreadFactory {
    override fun newThread(runnable: Runnable): Thread {
        val wrapperRunnable = Runnable {
            try {
                android.os.Process.setThreadPriority(mThreadPriority)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            runnable.run()
        }
        return Thread(wrapperRunnable)
    }
}