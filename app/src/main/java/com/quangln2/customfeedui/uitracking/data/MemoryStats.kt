package com.quangln2.customfeedui.uitracking.data


class MemoryStats {
    fun getUsedMemory(): Long {
        val runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
    }

}