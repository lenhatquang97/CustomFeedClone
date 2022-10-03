package com.quangln2.customfeed.models

class APoint(var x: Float, var y: Float) {
    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}

class RectanglePoint {
    var leftTop: APoint
    var rightBottom: APoint

    constructor(leftTop: APoint, rightBottom: APoint) {
        this.leftTop = leftTop
        this.rightBottom = rightBottom
    }

    constructor(left: Float, top: Float, right: Float, bottom: Float) {
        this.leftTop = APoint(left, top)
        this.rightBottom = APoint(right, bottom)
    }
}