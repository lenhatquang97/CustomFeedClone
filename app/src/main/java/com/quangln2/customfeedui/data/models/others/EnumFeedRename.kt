package com.quangln2.customfeedui.data.models.others

enum class EnumFeedLoadingCode(val value: Int) {
    SUCCESS(200),
    INITIAL(0),
    OFFLINE(-1)
}

enum class EnumFeedSplashScreenState(val value: Int){
    LOADING(1),
    COMPLETE(0),
    UNDEFINED(-1)
}