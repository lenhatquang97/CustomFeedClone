package com.quangln2.customfeedui.data.models.uimodel

class RectanglePoint(left: Float, top: Float, right: Float, bottom: Float) {
    var leftTop: APoint
    var rightBottom: APoint

    init {
        this.leftTop = APoint(left, top)
        this.rightBottom = APoint(right, bottom)
    }
}