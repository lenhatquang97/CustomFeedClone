package com.quangln2.customfeed.ui.customview.customgrid

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import com.quangln2.customfeed.data.models.uimodel.RectanglePoint
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.ui.customview.VideoThumbnailView

class CustomGridGroup : ViewGroup {
    val rectangles = mutableListOf<RectanglePoint>()
    private val contentPadding = 12

    var firstItemWidth = 0
    var firstItemHeight = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var itemNumber = childCount
        rectangles.clear()

        when (childCount) {
            0 -> return
            1 -> drawOneChild()
            2, 3, 4 -> {
                when (getChildAt(0)) {
                    is ImageView, is VideoThumbnailView, is FrameLayout, is VideoView, is LoadingVideoView -> {
                        if (firstItemWidth >= firstItemHeight) {
                            drawHorizontalGridMoreThanTwoAndLessThanFour()
                        } else {
                            drawVerticalGridMoreThanTwoAndLessThanFour()
                        }
                    }
                }

            }
            5 -> {
                when (getChildAt(0)) {
                    is ImageView, is VideoThumbnailView, is FrameLayout, is VideoView, is LoadingVideoView -> {
                        if (firstItemWidth >= firstItemHeight) {
                            drawHorizontalGridWithFive()
                        } else {
                            drawVerticalGridWithFive()
                        }
                    }
                }
            }
            6 -> drawSixChildren()
            7 -> drawSevenChildren()
            8 -> {
                when (getChildAt(0)) {
                    is ImageView, is VideoThumbnailView, is FrameLayout, is VideoView, is LoadingVideoView -> {
                        if (firstItemWidth >= firstItemHeight) {
                            drawHorizontalGridWithEight()
                        } else {
                            drawVerticalGridWithEight()
                        }
                    }
                }
            }
            else -> {
                drawNineChildren()
                itemNumber = 9
            }
        }

        for (i in 0 until itemNumber) {
            val child = getChildAt(i)
            if (child != null && child is ViewGroup) {
                val width = (rectangles[i].rightBottom.x.toInt()) - (rectangles[i].leftTop.x.toInt()) + contentPadding
                val height = (rectangles[i].rightBottom.y.toInt()) - (rectangles[i].leftTop.y.toInt()) + contentPadding
                child.measure(width, height)

                child.layout(
                    rectangles[i].leftTop.x.toInt() + contentPadding,
                    rectangles[i].leftTop.y.toInt() + contentPadding,
                    rectangles[i].rightBottom.x.toInt() - contentPadding,
                    rectangles[i].rightBottom.y.toInt() - contentPadding
                )
            } else {
                child?.layout(
                    rectangles[i].leftTop.x.toInt() + contentPadding,
                    rectangles[i].leftTop.y.toInt() + contentPadding,
                    rectangles[i].rightBottom.x.toInt() - contentPadding,
                    rectangles[i].rightBottom.y.toInt() - contentPadding
                )
            }
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    private fun drawOneChild() {
        val widthRectangle = width.toFloat()
        val largeRectangle = RectanglePoint(0f, 0f, widthRectangle, widthRectangle)
        rectangles.add(largeRectangle)
    }

    private fun drawSixChildren() {
        val widthRectangle = width.toFloat()
        val firstRect = RectanglePoint(0f, 0f, 2 * widthRectangle / 3, 2 * widthRectangle / 3)
        val secondRect = RectanglePoint(2 * widthRectangle / 3, 0f, widthRectangle, 1 * widthRectangle / 3)
        val thirdRect = RectanglePoint(2 * widthRectangle / 3, 1 * widthRectangle / 3, widthRectangle, 2 * widthRectangle / 3)

        rectangles.add(firstRect)
        rectangles.add(secondRect)
        rectangles.add(thirdRect)

        val top = 2 * widthRectangle / 3
        val bottom = widthRectangle
        for (i in 0 until 3) {
            val left = i * (widthRectangle / 3)
            val right = (i + 1) * (widthRectangle / 3)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
    }

    private fun drawSevenChildren() {
        val widthRectangle = width.toFloat()
        val firstRect = RectanglePoint(0f, 0f, 2 * widthRectangle / 3, 1 * widthRectangle / 3)
        val firstOfFirstRect =
            RectanglePoint(0f, widthRectangle / 3, 2 * widthRectangle / 3, 2 * widthRectangle / 3)
        val secondRect = RectanglePoint(2 * widthRectangle / 3, 0f, widthRectangle, 1 * widthRectangle / 3)
        val thirdRect = RectanglePoint(
            2 * widthRectangle / 3,
            1 * widthRectangle / 3,
            widthRectangle,
            2 * widthRectangle / 3
        )
        val top = 2 * widthRectangle / 3
        val bottom = widthRectangle

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

    private fun drawNineChildren() {
        val widthRectangle = width.toFloat()
        for (k in 0 until 3) {
            val top = k * widthRectangle / 3
            val bottom = (k + 1) * widthRectangle / 3
            for (i in 0 until 3) {
                val left = i * (widthRectangle / 3)
                val right = (i + 1) * (widthRectangle / 3)
                rectangles.add(RectanglePoint(left, top, right, bottom))
            }
        }
    }


}