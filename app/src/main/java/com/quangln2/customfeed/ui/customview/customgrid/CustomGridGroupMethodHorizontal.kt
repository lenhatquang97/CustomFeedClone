package com.quangln2.customfeed.ui.customview.customgrid

import com.quangln2.customfeed.data.models.uimodel.RectanglePoint

fun CustomGridGroup.drawHorizontalGridMoreThanTwoAndLessThanFour(){
    val widthRectangle = width.toFloat()
    if(childCount <= 4){
        val largeRectangle = RectanglePoint(0f, 0f, widthRectangle, widthRectangle / 2)
        val top = widthRectangle / 2
        val bottom = widthRectangle

        rectangles.add(largeRectangle)
        val remainingRectangles = childCount - 1
        for (i in 0 until remainingRectangles) {
            val left = i * (widthRectangle / remainingRectangles)
            val right = (i + 1) * (widthRectangle / remainingRectangles)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
    }
}

fun CustomGridGroup.drawHorizontalGridWithFive(){
    val widthRectangle = width.toFloat()
    val firstRect = RectanglePoint(0f, 0f, widthRectangle / 2, widthRectangle / 2)
    val secondRect = RectanglePoint(0f, widthRectangle / 2, widthRectangle / 2, widthRectangle)
    val left = widthRectangle / 2
    val right = widthRectangle

    rectangles.add(firstRect)
    rectangles.add(secondRect)

    for (i in 0 until 3) {
        val top = i * (widthRectangle / 3)
        val bottom = (i + 1) * (widthRectangle / 3)
        rectangles.add(RectanglePoint(left, top, right, bottom))
    }
}

fun CustomGridGroup.drawHorizontalGridWithEight(){
    val widthRectangle = width.toFloat()
    val largestRect = RectanglePoint(0f, 0f, 2 * widthRectangle / 3, 1 * widthRectangle / 3)
    val firstRect = RectanglePoint(2 * widthRectangle / 3, 0f, widthRectangle, 1 * widthRectangle / 3)

    rectangles.add(largestRect)
    rectangles.add(firstRect)

    for (k in 1 until 3) {
        val top = k * widthRectangle / 3
        val bottom = (k + 1) * widthRectangle / 3
        for (i in 0 until 3) {
            val left = i * (widthRectangle / 3)
            val right = (i + 1) * (widthRectangle / 3)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
    }

}