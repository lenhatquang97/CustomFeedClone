package com.quangln2.customfeed.others.callback

interface EventFeedCallback {
    fun onDeleteItem(id: String)
    fun onClickAddPost()
    fun onClickVideoView(currentVideoPosition: Long, url: String, listOfUrls: ArrayList<String>)
    fun onClickViewMore(id: String)
}