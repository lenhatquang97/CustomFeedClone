package com.quangln2.customfeed.ui.customview.customgrid

import com.quangln2.customfeed.data.models.uimodel.RectanglePoint

fun CustomGridGroup.drawVerticalGridMoreThanTwoAndLessThanFour() {
    val widthRectangle = width.toFloat()
    val largeRectangle = RectanglePoint(0f, 0f, widthRectangle / 2, widthRectangle)
    val left = widthRectangle / 2
    val right = widthRectangle

    rectangles.add(largeRectangle)
    val remainingRectangles = childCount - 1
    for (i in 0 until remainingRectangles) {
        val top = i * (widthRectangle / remainingRectangles)
        val bottom = (i + 1) * (widthRectangle / remainingRectangles)
        rectangles.add(RectanglePoint(left, top, right, bottom))
    }
}

fun CustomGridGroup.drawVerticalGridWithFive() {
    val widthRectangle = width.toFloat()
    val firstRect = RectanglePoint(0f, 0f, widthRectangle / 2, 2 * widthRectangle / 3)
    val secondRect = RectanglePoint(widthRectangle / 2, 0f, widthRectangle, 2 * widthRectangle / 3)
    val top = 2 * widthRectangle / 3
    val bottom = widthRectangle

    rectangles.add(firstRect)
    rectangles.add(secondRect)

    for (i in 0 until 3) {
        val left = i * (widthRectangle / 3)
        val right = (i + 1) * (widthRectangle / 3)
        rectangles.add(RectanglePoint(left, top, right, bottom))
    }
}

fun CustomGridGroup.drawVerticalGridWithEight() {
    val widthRectangle = width.toFloat()
    val largestRect = RectanglePoint(0f, 0f, 1 * widthRectangle / 3, 2 * widthRectangle / 3)
    val firstRect = RectanglePoint(widthRectangle / 3, 0f, 2 * widthRectangle / 3, 1 * widthRectangle / 3)
    val firstOfFirstRect = RectanglePoint(
        widthRectangle / 3,
        widthRectangle / 3,
        2 * widthRectangle / 3,
        2 * widthRectangle / 3
    )
    val secondRect = RectanglePoint(2 * widthRectangle / 3, 0f, widthRectangle, 1 * widthRectangle / 3)
    val thirdRect = RectanglePoint(
        2 * widthRectangle / 3,
        1 * widthRectangle / 3,
        widthRectangle,
        2 * widthRectangle / 3
    )
    val top = 2 * widthRectangle / 3
    val bottom = widthRectangle

    rectangles.add(largestRect)
    rectangles.add(firstRect)
    rectangles.add(firstOfFirstRect)
    rectangles.add(secondRect)
    rectangles.add(thirdRect)

    for (i in 0 until 3) {
        val left = i * (widthRectangle / 3)
        val right = (i + 1) * (widthRectangle / 3)
        rectangles.add(RectanglePoint(left, top, right, bottom))
    }
}