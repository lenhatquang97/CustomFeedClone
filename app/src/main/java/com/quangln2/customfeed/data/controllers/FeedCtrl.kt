package com.quangln2.customfeed.data.controllers

import java.util.*

object FeedCtrl {
    val videoDeque: Deque<Pair<Int, Int>> = LinkedList()


    fun addToFirst(itemPosition: Int, i: Int) = videoDeque.addFirst(Pair(itemPosition, i))
    fun addToLast(itemPosition: Int, i: Int) = videoDeque.addLast(Pair(itemPosition, i))
    fun isEmpty(): Boolean = videoDeque.isEmpty()
    fun peekFirst(): Pair<Int, Int> = if(videoDeque.isEmpty()) Pair(-1, -1) else videoDeque.peekFirst()!!
    fun peekLast(): Pair<Int, Int> = if(videoDeque.isEmpty()) Pair(-1, -1) else videoDeque.peekLast()!!
    fun popFirstSafely(){
        if(videoDeque.isNotEmpty()) videoDeque.removeFirst()
    }

}