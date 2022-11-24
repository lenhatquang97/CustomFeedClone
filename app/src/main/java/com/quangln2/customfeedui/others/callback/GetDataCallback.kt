package com.quangln2.customfeedui.others.callback

import com.quangln2.customfeedui.data.models.datamodel.MyPost

interface GetDataCallback {
    fun onGetFeedLoadingCode(loadingCode: Int)
    fun onGetUploadList(postList: List<MyPost>)
}