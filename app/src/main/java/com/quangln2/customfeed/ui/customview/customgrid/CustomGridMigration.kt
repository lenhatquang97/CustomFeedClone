package com.quangln2.customfeed.ui.customview.customgrid

import com.quangln2.customfeed.data.models.uimodel.RectanglePoint

object CustomGridMigration {
    fun drawOneChildGrid(): List<RectanglePoint> {
        return listOf(
            RectanglePoint(0f, 0f, 1f, 1f)
        )

    }

    fun drawSixChildrenGrid(): List<RectanglePoint> {
        val rectangles = mutableListOf<RectanglePoint>()
        val firstRect = RectanglePoint(0f, 0f, 2 / 3f, 2 / 3f)
        val secondRect = RectanglePoint(2 / 3f, 0f, 1f, 1 / 3f)
        val thirdRect = RectanglePoint(2 / 3f, 1 / 3f, 1f, 2 / 3f)
        rectangles.apply {
            add(firstRect)
            add(secondRect)
            add(thirdRect)
        }
        val top = 2 / 3f
        val bottom = 1f
        for (i in 0 until 3) {
            val left = i * (1 / 3f)
            val right = (i + 1) * (1 / 3f)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
        return rectangles
    }

    fun drawSevenChildrenGrid(): List<RectanglePoint> {
        val rectangles = mutableListOf<RectanglePoint>()
        val firstRect = RectanglePoint(0f, 0f, 2 / 3f, 1 / 3f)
        val firstOfFirstRect = RectanglePoint(0f, 1 / 3f, 2 / 3f, 2 / 3f)
        val secondRect = RectanglePoint(2 / 3f, 0f, 1f, 1 / 3f)
        val thirdRect = RectanglePoint(2 / 3f, 1 / 3f, 1f, 2 / 3f)
        val top = 2 / 3f
        val bottom = 1f
        rectangles.apply {
            add(firstRect)
            add(firstOfFirstRect)
            add(secondRect)
            add(thirdRect)
        }
        for (i in 0 until 3) {
            val left = i * (1 / 3f)
            val right = (i + 1) * (1 / 3f)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
        return rectangles
    }

    fun drawNineChildrenGrid(): List<RectanglePoint> {
        val rectangles = mutableListOf<RectanglePoint>()
        for (k in 0 until 3) {
            val top = k * 1 / 3f
            val bottom = (k + 1) * 1 / 3f
            for (i in 0 until 3) {
                val left = i * (1 / 3f)
                val right = (i + 1) * (1 / 3f)
                rectangles.add(RectanglePoint(left, top, right, bottom))
            }
        }
        return rectangles
    }

    fun drawMoreThanTenChildrenGrid(childCount: Int): List<RectanglePoint> {
        val rectangles = mutableListOf<RectanglePoint>()
        val loopNumber = childCount / 3
        val remainingItemNumber = childCount % 3
        for (k in 0 until loopNumber) {
            val top = k * 1 / 3f
            val bottom = (k + 1) * 1 / 3f
            for (i in 0 until 3) {
                val left = i * (1 / 3f)
                val right = (i + 1) * (1 / 3f)
                rectangles.add(RectanglePoint(left, top, right, bottom))
            }
        }
        val top = loopNumber / 3f
        val bottom = (loopNumber + 1) / 3f
        for (k in 0 until remainingItemNumber) {
            val left = k * (1 / 3f)
            val right = (k + 1) * (1 / 3f)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
        return rectangles
    }

    fun drawHorizontalGridMoreThanTwoAndLessThanFourGrid(childCount: Int): List<RectanglePoint> {
        val rectangles = mutableListOf<RectanglePoint>()
        val largeRectangle = RectanglePoint(0f, 0f, 1f, 0.5f)
        val top = 0.5f
        val bottom = 1f

        rectangles.add(largeRectangle)
        val remainingRectangles = childCount - 1
        for (i in 0 until remainingRectangles) {
            val left = i * (1f / remainingRectangles)
            val right = (i + 1) * (1f / remainingRectangles)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
        return rectangles
    }

    fun drawHorizontalGridWithFiveGrid(): List<RectanglePoint> {
        val rectangles = mutableListOf<RectanglePoint>()
        val firstRect = RectanglePoint(0f, 0f, 1 / 2f, 1 / 2f)
        val secondRect = RectanglePoint(0f, 1 / 2f, 1 / 2f, 1f)
        val left = 0.5f
        val right = 1f

        rectangles.add(firstRect)
        rectangles.add(secondRect)

        for (i in 0 until 3) {
            val top = i * (1 / 3f)
            val bottom = (i + 1) * (1 / 3f)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
        return rectangles
    }

    fun drawHorizontalGridWithEightGrid(): List<RectanglePoint> {
        val rectangles = mutableListOf<RectanglePoint>()
        val largestRect = RectanglePoint(0f, 0f, 2 * 1f / 3, 1 * 1f / 3)
        val firstRect = RectanglePoint(2 * 1f / 3, 0f, 1f, 1 * 1f / 3)

        rectangles.add(largestRect)
        rectangles.add(firstRect)

        for (k in 1 until 3) {
            val top = k * 1f / 3
            val bottom = (k + 1) * 1f / 3
            for (i in 0 until 3) {
                val left = i * (1f / 3)
                val right = (i + 1) * (1f / 3)
                rectangles.add(RectanglePoint(left, top, right, bottom))
            }
        }
        return rectangles

    }

    fun drawVerticalGridMoreThanTwoAndLessThanFourGrid(childCount: Int): List<RectanglePoint> {
        val rectangles = mutableListOf<RectanglePoint>()
        val largeRectangle = RectanglePoint(0f, 0f, 1f / 2, 1f)
        val left = 1f / 2
        val right = 1f

        rectangles.add(largeRectangle)
        val remainingRectangles = childCount - 1
        for (i in 0 until remainingRectangles) {
            val top = i * (1f / remainingRectangles)
            val bottom = (i + 1) * (1f / remainingRectangles)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
        return rectangles
    }

    fun drawVerticalGridWithFiveGrid(): List<RectanglePoint> {
        val rectangles = mutableListOf<RectanglePoint>()
        val firstRect = RectanglePoint(0f, 0f, 1f / 2, 2 * 1f / 3)
        val secondRect = RectanglePoint(1f / 2, 0f, 1f, 2 * 1f / 3)
        val top = 2 * 1f / 3
        val bottom = 1f

        rectangles.add(firstRect)
        rectangles.add(secondRect)

        for (i in 0 until 3) {
            val left = i * (1f / 3)
            val right = (i + 1) * (1f / 3)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
        return rectangles
    }

    fun drawVerticalGridWithEightGrid(): List<RectanglePoint> {
        val rectangles = mutableListOf<RectanglePoint>()
        val largestRect = RectanglePoint(0f, 0f, 1 * 1f / 3, 2 * 1f / 3)
        val firstRect = RectanglePoint(1f / 3, 0f, 2 * 1f / 3, 1 * 1f / 3)
        val firstOfFirstRect = RectanglePoint(
            1f / 3,
            1f / 3,
            2 * 1f / 3,
            2 * 1f / 3
        )
        val secondRect = RectanglePoint(2 * 1f / 3, 0f, 1f, 1 * 1f / 3)
        val thirdRect = RectanglePoint(
            2 * 1f / 3,
            1 * 1f / 3,
            1f,
            2 * 1f / 3
        )
        val top = 2 * 1f / 3
        val bottom = 1f

        rectangles.apply {
            add(largestRect)
            add(firstRect)
            add(firstOfFirstRect)
            add(secondRect)
            add(thirdRect)
        }

        for (i in 0 until 3) {
            val left = i * (1f / 3)
            val right = (i + 1) * (1f / 3)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
        return rectangles
    }
}

