package com.quangln2.customfeed.data.controllers

import androidx.lifecycle.MutableLiveData
import java.util.*

object FeedCtrl {
    val videoDeque: Deque<Pair<Int, Int>> = LinkedList()
    val playingQueue: Queue<Pair<Int, Int>> = LinkedList()
    var isLoadingToUpload = MutableLiveData<Int>().apply { value = -1 }

    fun addToLast(itemPosition: Int, i: Int) = videoDeque.addLast(Pair(itemPosition, i))
    fun isEmpty(): Boolean = videoDeque.isEmpty()
    fun peekFirst(): Pair<Int, Int> = if(videoDeque.isEmpty()) Pair(-1, -1) else videoDeque.peekFirst()!!
    fun peekLast(): Pair<Int, Int> = if(videoDeque.isEmpty()) Pair(-1, -1) else videoDeque.peekLast()!!
    fun popFirstSafely(){
        if(videoDeque.isNotEmpty()) videoDeque.removeFirst()
    }

    fun compareDequeWithList(list: List<Pair<Int, Int>>): Boolean{
        if(list.size != videoDeque.size) return false
        for(i in list.indices){
            if(list[i] != videoDeque.elementAt(i)) return false
        }
        return true
    }


}