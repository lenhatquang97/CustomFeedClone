package com.quangln2.customfeedui.uitracking.data

class MemoryStats {
    fun getMaxMemory(): Long = Runtime.getRuntime().maxMemory() / 1048576L
    fun getTotalMemory(): Long = Runtime.getRuntime().totalMemory() / 1048576L
    fun getUsedMemory(): Long {
        val runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
    }
}