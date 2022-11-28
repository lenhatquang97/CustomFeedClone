package com.quangln2.customfeedui.others.callback

import com.quangln2.customfeedui.data.models.uimodel.CurrentVideo

interface EventFeedCallback {
    fun onDeleteItem(id: String, position: Int)
    fun onClickAddPost()
    fun onClickVideoView(currentVideo: CurrentVideo)
    fun onClickViewMore(id: String)
}