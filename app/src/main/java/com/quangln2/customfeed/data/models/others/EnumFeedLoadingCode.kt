package com.quangln2.customfeed.data.models.others

enum class EnumFeedLoadingCode(val value: Int) {
    SUCCESS(200),
    INITIAL(0),
    OFFLINE(-1)
}

//1 means loading, 0 means complete loading, but -1 means undefined
enum class EnumFeedSplashScreenState(val value: Int){
    LOADING(1),
    COMPLETE(0),
    UNDEFINED(-1)
}