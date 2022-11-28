package com.quangln2.customfeedui.others.callback

import android.view.View
import com.quangln2.customfeedui.data.models.uimodel.CurrentVideo

interface EventFeedCallback {
    fun onDeleteItem(id: String, position: Int)
    fun onClickAddPost()
    fun onClickVideoView(currentVideo: CurrentVideo)
    fun onClickViewMore(id: String)
    fun onRecycled(child: View)
}