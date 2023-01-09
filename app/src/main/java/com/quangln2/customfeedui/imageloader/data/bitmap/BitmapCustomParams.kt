package com.quangln2.customfeedui.imageloader.data.bitmap

enum class TypeImage{
    Image,
    VideoThumbnail
}

data class BitmapCustomParams(
    var countRef: Boolean = true,
    var isFullScreen: Boolean = false,
    var folderName: String = "",
    var type: TypeImage = TypeImage.Image
)