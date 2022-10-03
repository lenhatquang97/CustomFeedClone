package com.quangln2.customfeed.callback

interface EventFeedCallback {
    fun onDeleteItem(id: String)
    fun onClickAddPost()
    fun onClickVideoView(url: String)
    fun onClickViewMore(id: String)
}