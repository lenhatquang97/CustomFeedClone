package com.quangln2.customfeedui.others.callback

interface EventFeedCallback {
    fun onDeleteItem(id: String, position: Int)
    fun onClickAddPost()
    fun onClickVideoView(currentVideoPosition: Long, url: String, listOfUrls: ArrayList<String>)
    fun onClickViewMore(id: String)
}