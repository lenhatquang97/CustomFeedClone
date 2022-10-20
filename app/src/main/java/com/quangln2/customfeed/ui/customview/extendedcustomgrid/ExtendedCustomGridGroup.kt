package com.quangln2.customfeed.ui.customview.extendedcustomgrid

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import com.quangln2.customfeed.data.models.uimodel.RectanglePoint
import com.quangln2.customfeed.ui.customview.LoadingVideoView

class ExtendedCustomGridGroup : ViewGroup {
    val rectangles = mutableListOf<RectanglePoint>()
    private val contentPadding = 5

    var firstItemWidth = 0
    var firstItemHeight = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        rectangles.clear()
        when (childCount) {
            0 -> return
            1 -> drawOneChild()
            2, 3, 4 -> {
                when (getChildAt(0)) {
                    is ImageView -> {
                        if (firstItemWidth > firstItemHeight) {
                            drawHorizontalGridMoreThanTwoAndLessThanFour()
                        } else {
                            drawVerticalGridMoreThanTwoAndLessThanFour()
                        }
                    }
                    is VideoView, is LoadingVideoView -> {
                        drawHorizontalGridMoreThanTwoAndLessThanFour()
                    }
                }

            }
            5 -> {
                when (getChildAt(0)) {
                    is ImageView -> {
                        if (firstItemWidth > firstItemHeight) {
                            drawHorizontalGridWithFive()
                        } else {
                            drawVerticalGridWithFive()
                        }
                    }
                    is VideoView, is LoadingVideoView -> {
                        drawHorizontalGridWithFive()
                    }
                }
            }
            6 -> drawSixChildren()
            7 -> drawSevenChildren()
            8 -> {
                when (getChildAt(0)) {
                    is ImageView -> {
                        if (firstItemWidth > firstItemHeight) {
                            drawHorizontalGridWithEight()
                        } else {
                            drawVerticalGridWithEight()
                        }
                    }
                    is VideoView, is LoadingVideoView -> {
                        drawHorizontalGridWithEight()
                    }
                }
            }
            else -> {
                drawMoreThanNineChildren()
            }
        }

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.layout(
                rectangles[i].leftTop.x.toInt() + contentPadding,
                rectangles[i].leftTop.y.toInt() + contentPadding,
                rectangles[i].rightBottom.x.toInt() - contentPadding,
                rectangles[i].rightBottom.y.toInt() - contentPadding
            )
        }


    }

    private fun drawOneChild() {
        val widthGrid = width.toFloat()
        val largeRectangle = RectanglePoint(0f, 0f, widthGrid, widthGrid)
        rectangles.add(largeRectangle)
    }

    private fun drawSixChildren() {
        val widthGrid = width.toFloat()
        val firstRect = RectanglePoint(0f, 0f, 2 * widthGrid / 3, 2 * widthGrid / 3)
        val secondRect = RectanglePoint(2 * widthGrid / 3, 0f, widthGrid, 1 * widthGrid / 3)
        val thirdRect = RectanglePoint(
            2 * widthGrid / 3,
            1 * widthGrid / 3,
            widthGrid,
            2 * widthGrid / 3
        )

        rectangles.add(firstRect)
        rectangles.add(secondRect)
        rectangles.add(thirdRect)

        val top = 2 * widthGrid / 3
        val bottom = widthGrid
        for (i in 0 until 3) {
            val left = i * (widthGrid / 3)
            val right = (i + 1) * (widthGrid / 3)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
    }

    private fun drawSevenChildren() {
        val widthGrid = width.toFloat()
        val firstRect = RectanglePoint(0f, 0f, 2 * widthGrid / 3, 1 * widthGrid / 3)
        val firstOfFirstRect =
            RectanglePoint(0f, widthGrid / 3, 2 * widthGrid / 3, 2 * widthGrid / 3)
        val secondRect = RectanglePoint(2 * widthGrid / 3, 0f, widthGrid, 1 * widthGrid / 3)
        val thirdRect = RectanglePoint(
            2 * widthGrid / 3,
            1 * widthGrid / 3,
            widthGrid,
            2 * widthGrid / 3
        )
        val top = 2 * widthGrid / 3
        val bottom = widthGrid

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

    private fun drawMoreThanNineChildren() {
        val widthGrid = width.toFloat()
        val loopNumber = childCount / 3
        val remainingItemNumber = childCount % 3
        for (k in 0 until loopNumber) {
            val top = k * widthGrid / 3
            val bottom = (k + 1) * widthGrid / 3
            for (i in 0 until 3) {
                val left = i * (widthGrid / 3)
                val right = (i + 1) * (widthGrid / 3)
                rectangles.add(RectanglePoint(left, top, right, bottom))
            }
        }
        val top = loopNumber * widthGrid / 3
        val bottom = (loopNumber + 1) * widthGrid / 3
        for (k in 0 until remainingItemNumber) {
            val left = k * (widthGrid / 3)
            val right = (k + 1) * (widthGrid / 3)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }


}