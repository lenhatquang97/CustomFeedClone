package com.quangln2.customfeedui.data.models.uimodel

import android.graphics.PointF

class RectanglePoint(left: Float, top: Float, right: Float, bottom: Float) {
    var leftTop: PointF
    var rightBottom: PointF

    init {
        this.leftTop = PointF(left, top)
        this.rightBottom = PointF(right, bottom)
    }
}