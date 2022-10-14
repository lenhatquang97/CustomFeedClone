package com.quangln2.customfeed.ui.customview.extendedcustomgrid

import com.quangln2.customfeed.data.models.uimodel.RectanglePoint

fun ExtendedCustomGridGroup.drawHorizontalGridMoreThanTwoAndLessThanFour() {
    val widthGrid = width.toFloat()
    val largeRectangle = RectanglePoint(0f, 0f, widthGrid, widthGrid / 2)
    val top = widthGrid / 2
    val bottom = widthGrid

    rectangles.add(largeRectangle)
    val remainingRectangles = childCount - 1
    for (i in 0 until remainingRectangles) {
        val left = i * (widthGrid / remainingRectangles)
        val right = (i + 1) * (widthGrid / remainingRectangles)
        rectangles.add(RectanglePoint(left, top, right, bottom))
    }
}

fun ExtendedCustomGridGroup.drawHorizontalGridWithFive() {
    val widthGrid = width.toFloat()
    val firstRect = RectanglePoint(0f, 0f, widthGrid / 2, widthGrid / 2)
    val secondRect = RectanglePoint(0f, widthGrid / 2, widthGrid / 2, widthGrid)
    val left = widthGrid / 2
    val right = widthGrid

    rectangles.add(firstRect)
    rectangles.add(secondRect)

    for (i in 0 until 3) {
        val top = i * (widthGrid / 3)
        val bottom = (i + 1) * (widthGrid / 3)
        rectangles.add(RectanglePoint(left, top, right, bottom))
    }
}

fun ExtendedCustomGridGroup.drawHorizontalGridWithEight() {
    val widthGrid = width.toFloat()
    val largestRect = RectanglePoint(0f, 0f, 2 * widthGrid / 3, 1 * widthGrid / 3)
    val firstRect = RectanglePoint(2 * widthGrid / 3, 0f, widthGrid, 1 * widthGrid / 3)

    rectangles.add(largestRect)
    rectangles.add(firstRect)

    for (k in 1 until 3) {
        val top = k * widthGrid / 3
        val bottom = (k + 1) * widthGrid / 3
        for (i in 0 until 3) {
            val left = i * (widthGrid / 3)
            val right = (i + 1) * (widthGrid / 3)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
    }
}