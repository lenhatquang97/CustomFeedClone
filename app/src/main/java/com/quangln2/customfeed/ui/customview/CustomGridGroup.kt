package com.quangln2.customfeed.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import com.quangln2.customfeed.data.models.RectanglePoint

class CustomGridGroup : ViewGroup {
    private val rectangles = mutableListOf<RectanglePoint>()
    private val contentPadding = 5
    private var itemNumber = 0

    var firstWidth = 0
    var firstHeight = 0

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount >= 1) {
            val firstChild = getChildAt(0)
            if (firstChild is ImageView) {
                if (firstWidth > firstHeight) {
                    drawHorizontalGrid()
                } else {
                    drawVerticalGrid()
                }
            } else if (firstChild is VideoView || firstChild is LoadingVideoView) {
                drawHorizontalGrid()
            }
        }

        for (i in 0 until itemNumber) {
            val child = getChildAt(i)
            if (child is ViewGroup) {
                val width =
                    (rectangles[i].rightBottom.x.toInt()) - (rectangles[i].leftTop.x.toInt()) + 2 * contentPadding
                val height =
                    (rectangles[i].rightBottom.y.toInt()) - (rectangles[i].leftTop.y.toInt()) + 2 * contentPadding
                measureChild(child, width, height)
                child.layout(
                    rectangles[i].leftTop.x.toInt(),
                    rectangles[i].leftTop.y.toInt(),
                    rectangles[i].rightBottom.x.toInt(),
                    rectangles[i].rightBottom.y.toInt()
                )
            } else {
                child.layout(
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

    private fun drawHorizontalGrid() {
        itemNumber = childCount
        rectangles.clear()
        if (childCount == 1) {
            drawOneChild()
        } else if (childCount <= 4) {
            val largeRectangle = RectanglePoint(0f, 0f, width.toFloat(), width.toFloat() / 2)
            val top = width.toFloat() / 2
            val bottom = width.toFloat()

            rectangles.add(largeRectangle)
            val remainingRectangles = childCount - 1
            for (i in 0 until remainingRectangles) {
                val left = i * (width.toFloat() / remainingRectangles)
                val right = (i + 1) * (width.toFloat() / remainingRectangles)
                rectangles.add(RectanglePoint(left, top, right, bottom))
            }
        } else if (childCount == 5) {
            val firstRect = RectanglePoint(0f, 0f, width.toFloat() / 2, width.toFloat() / 2)
            val secondRect = RectanglePoint(0f, width.toFloat() / 2, width.toFloat() / 2, width.toFloat())
            val left = width.toFloat() / 2
            val right = width.toFloat()

            rectangles.add(firstRect)
            rectangles.add(secondRect)

            for (i in 0 until 3) {
                val top = i * (width.toFloat() / 3)
                val bottom = (i + 1) * (width.toFloat() / 3)
                rectangles.add(RectanglePoint(left, top, right, bottom))
            }

        } else if (childCount == 6) {
            drawSixChildren()

        } else if (childCount == 7) {
            drawSevenChildren()

        } else if (childCount == 8) {
            val largestRect = RectanglePoint(0f, 0f, 2 * width.toFloat() / 3, 1 * width.toFloat() / 3)
            val firstRect = RectanglePoint(2 * width.toFloat() / 3, 0f, width.toFloat(), 1 * width.toFloat() / 3)

            rectangles.add(largestRect)
            rectangles.add(firstRect)

            for (k in 1 until 3) {
                val top = k * width.toFloat() / 3
                val bottom = (k + 1) * width.toFloat() / 3
                for (i in 0 until 3) {
                    val left = i * (width.toFloat() / 3)
                    val right = (i + 1) * (width.toFloat() / 3)
                    rectangles.add(RectanglePoint(left, top, right, bottom))
                }
            }

        } else if (childCount >= 9) {
            drawNineChildren()
            itemNumber = 9
        }
    }

    private fun drawVerticalGrid() {
        itemNumber = childCount
        rectangles.clear()
        if (childCount == 1) {
            drawOneChild()
        } else if (childCount <= 4) {
            val largeRectangle = RectanglePoint(0f, 0f, width.toFloat() / 2, width.toFloat())
            val left = width.toFloat() / 2
            val right = width.toFloat()

            rectangles.add(largeRectangle)
            val remainingRectangles = childCount - 1
            for (i in 0 until remainingRectangles) {
                val top = i * (width.toFloat() / remainingRectangles)
                val bottom = (i + 1) * (width.toFloat() / remainingRectangles)
                rectangles.add(RectanglePoint(left, top, right, bottom))
            }
        } else if (childCount == 5) {
            val firstRect = RectanglePoint(0f, 0f, width.toFloat() / 2, 2 * width.toFloat() / 3)
            val secondRect = RectanglePoint(width.toFloat() / 2, 0f, width.toFloat(), 2 * width.toFloat() / 3)
            val top = 2 * width.toFloat() / 3
            val bottom = width.toFloat()

            rectangles.add(firstRect)
            rectangles.add(secondRect)

            for (i in 0 until 3) {
                val left = i * (width.toFloat() / 3)
                val right = (i + 1) * (width.toFloat() / 3)
                rectangles.add(RectanglePoint(left, top, right, bottom))
            }

        } else if (childCount == 6) {
            drawSixChildren()

        } else if (childCount == 7) {
            drawSevenChildren()

        } else if (childCount == 8) {
            val largestRect = RectanglePoint(0f, 0f, 1 * width.toFloat() / 3, 2 * width.toFloat() / 3)
            val firstRect = RectanglePoint(width.toFloat() / 3, 0f, 2 * width.toFloat() / 3, 1 * width.toFloat() / 3)
            val firstOfFirstRect = RectanglePoint(
                width.toFloat() / 3,
                width.toFloat() / 3,
                2 * width.toFloat() / 3,
                2 * width.toFloat() / 3
            )
            val secondRect = RectanglePoint(2 * width.toFloat() / 3, 0f, width.toFloat(), 1 * width.toFloat() / 3)
            val thirdRect = RectanglePoint(
                2 * width.toFloat() / 3,
                1 * width.toFloat() / 3,
                width.toFloat(),
                2 * width.toFloat() / 3
            )
            val top = 2 * width.toFloat() / 3
            val bottom = width.toFloat()

            rectangles.add(largestRect)
            rectangles.add(firstRect)
            rectangles.add(firstOfFirstRect)
            rectangles.add(secondRect)
            rectangles.add(thirdRect)

            for (i in 0 until 3) {
                val left = i * (width.toFloat() / 3)
                val right = (i + 1) * (width.toFloat() / 3)
                rectangles.add(RectanglePoint(left, top, right, bottom))
            }

        } else if (childCount >= 9) {
            drawNineChildren()
            itemNumber = 9
        }
    }

    private fun drawOneChild() {
        val largeRectangle = RectanglePoint(0f, 0f, width.toFloat(), width.toFloat())
        rectangles.add(largeRectangle)
    }

    private fun drawSixChildren() {
        val firstRect = RectanglePoint(0f, 0f, 2 * width.toFloat() / 3, 2 * width.toFloat() / 3)
        val secondRect = RectanglePoint(2 * width.toFloat() / 3, 0f, width.toFloat(), 1 * width.toFloat() / 3)
        val thirdRect = RectanglePoint(
            2 * width.toFloat() / 3,
            1 * width.toFloat() / 3,
            width.toFloat(),
            2 * width.toFloat() / 3
        )

        rectangles.add(firstRect)
        rectangles.add(secondRect)
        rectangles.add(thirdRect)

        val top = 2 * width.toFloat() / 3
        val bottom = width.toFloat()
        for (i in 0 until 3) {
            val left = i * (width.toFloat() / 3)
            val right = (i + 1) * (width.toFloat() / 3)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
    }

    private fun drawSevenChildren() {
        val firstRect = RectanglePoint(0f, 0f, 2 * width.toFloat() / 3, 1 * width.toFloat() / 3)
        val firstOfFirstRect =
            RectanglePoint(0f, width.toFloat() / 3, 2 * width.toFloat() / 3, 2 * width.toFloat() / 3)
        val secondRect = RectanglePoint(2 * width.toFloat() / 3, 0f, width.toFloat(), 1 * width.toFloat() / 3)
        val thirdRect = RectanglePoint(
            2 * width.toFloat() / 3,
            1 * width.toFloat() / 3,
            width.toFloat(),
            2 * width.toFloat() / 3
        )
        val top = 2 * width.toFloat() / 3
        val bottom = width.toFloat()

        rectangles.add(firstRect)
        rectangles.add(firstOfFirstRect)
        rectangles.add(secondRect)
        rectangles.add(thirdRect)

        for (i in 0 until 3) {
            val left = i * (width.toFloat() / 3)
            val right = (i + 1) * (width.toFloat() / 3)
            rectangles.add(RectanglePoint(left, top, right, bottom))
        }
    }

    private fun drawNineChildren() {
        for (k in 0 until 3) {
            val top = k * width.toFloat() / 3
            val bottom = (k + 1) * width.toFloat() / 3
            for (i in 0 until 3) {
                val left = i * (width.toFloat() / 3)
                val right = (i + 1) * (width.toFloat() / 3)
                rectangles.add(RectanglePoint(left, top, right, bottom))
            }
        }
    }

//    override fun measureChildWithMargins(
//        child: View,
//        parentWidthMeasureSpec: Int,
//        widthUsed: Int,
//        parentHeightMeasureSpec: Int,
//        heightUsed: Int
//    ) {
//        val lp = child.layoutParams as MarginLayoutParams
//        val childWidthMeasureSpec = getChildMeasureSpec(
//            parentWidthMeasureSpec,
//            widthUsed + lp.leftMargin + lp.rightMargin,
//            lp.width
//        )
//        val childHeightMeasureSpec = getChildMeasureSpec(
//            parentHeightMeasureSpec,
//            heightUsed + lp.topMargin + lp.bottomMargin,
//            lp.height
//        )
//        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
//    }


}