package com.quangln2.customfeed.ui.customview.extendedcustomgrid

import com.quangln2.customfeed.data.models.uimodel.RectanglePoint

fun ExtendedCustomGridGroup.drawVerticalGridMoreThanTwoAndLessThanFour() {
    val widthGrid = width.toFloat()
    val largeRectangle = RectanglePoint(0f, 0f, widthGrid / 2, widthGrid)
    val left = widthGrid / 2
    val right = widthGrid

    rectangles.add(largeRectangle)
    val remainingRectangles = childCount - 1
    for (i in 0 until remainingRectangles) {
        val top = i * (widthGrid / remainingRectangles)
        val bottom = (i + 1) * (widthGrid / remainingRectangles)
        rectangles.add(RectanglePoint(left, top, right, bottom))
    }
}

fun ExtendedCustomGridGroup.drawVerticalGridWithFive() {
    val widthGrid = width.toFloat()
    val firstRect = RectanglePoint(0f, 0f, widthGrid / 2, 2 * widthGrid / 3)
    val secondRect = RectanglePoint(widthGrid / 2, 0f, widthGrid, 2 * widthGrid / 3)
    val top = 2 * widthGrid / 3
    val bottom = widthGrid

    rectangles.add(firstRect)
    rectangles.add(secondRect)

    for (i in 0 until 3) {
        val left = i * (widthGrid / 3)
        val right = (i + 1) * (widthGrid / 3)
        rectangles.add(RectanglePoint(left, top, right, bottom))
    }
}

fun ExtendedCustomGridGroup.drawVerticalGridWithEight() {
    val widthGrid = width.toFloat()
    val largestRect = RectanglePoint(0f, 0f, 1 * widthGrid / 3, 2 * widthGrid / 3)
    val firstRect = RectanglePoint(widthGrid / 3, 0f, 2 * widthGrid / 3, 1 * widthGrid / 3)
    val firstOfFirstRect = RectanglePoint(
        widthGrid / 3,
        widthGrid / 3,
        2 * widthGrid / 3,
        2 * widthGrid / 3
    )
    val secondRect = RectanglePoint(2 * widthGrid / 3, 0f, widthGrid, 1 * widthGrid / 3)
    val thirdRect = RectanglePoint(
        2 * widthGrid / 3,
        1 * widthGrid / 3,
        widthGrid,
        2 * widthGrid / 3
    )
    val top = 2 * widthGrid / 3
    val bottom = widthGrid

    rectangles.add(largestRect)
    rectangles.add(firstRect)
    rectangles.add(firstOfFirstRect)
    rectangles.add(secondRect)
    rectangles.add(thirdRect)

    for (i in 0 until 3) {
        val left = i * (widthGrid / 3)
        val right = (i + 1) * (widthGrid / 3)
        rectangles.add(RectanglePoint(left, top, right, bottom))
    }
}