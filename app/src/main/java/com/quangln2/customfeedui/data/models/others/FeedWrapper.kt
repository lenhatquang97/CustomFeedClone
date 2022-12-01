package com.quangln2.customfeedui.data.models.others

import com.quangln2.customfeedui.data.models.datamodel.MyPost

data class FeedWrapper(val feedList: List<MyPost>, val loadingCode: Int)