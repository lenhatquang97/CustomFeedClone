package com.quangln2.customfeed.others.extension

import android.graphics.Rect
import android.view.View
import android.widget.ScrollView
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.isViewVisible(view: View): Boolean {
    val scrollBounds = Rect()
    this.getDrawingRect(scrollBounds)
    var top = 0f
    var temp = view
    while (temp !is ScrollView){
        top += (temp).y
        temp = temp.parent as View
    }
    val bottom = top + view.height
    return scrollBounds.top < top && scrollBounds.bottom > bottom
}