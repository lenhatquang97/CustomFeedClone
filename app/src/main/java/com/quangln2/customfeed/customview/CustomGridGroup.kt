package com.quangln2.customfeed.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

class CustomGridGroup : ViewGroup {
    private val rectangles = mutableListOf<RectanglePoint>()
    private val contentPadding = 5

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var itemNumber = childCount
        rectangles.clear()
        if (childCount == 1) {
            val largeRectangle = RectanglePoint(0f, 0f, width.toFloat(), width.toFloat())
            rectangles.add(largeRectangle)
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

        } else if (childCount == 7) {
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
            for (k in 0 until 3) {
                val top = k * width.toFloat() / 3
                val bottom = (k + 1) * width.toFloat() / 3
                for (i in 0 until 3) {
                    val left = i * (width.toFloat() / 3)
                    val right = (i + 1) * (width.toFloat() / 3)
                    rectangles.add(RectanglePoint(left, top, right, bottom))
                }
            }
            itemNumber = 9
        }

        for (i in 0 until itemNumber) {
            val child = getChildAt(i)
            child.layout(
                rectangles[i].leftTop.x.toInt() + contentPadding,
                rectangles[i].leftTop.y.toInt() + contentPadding,
                rectangles[i].rightBottom.x.toInt() - contentPadding,
                rectangles[i].rightBottom.y.toInt() - contentPadding
            )
        }


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec)
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    override fun measureChildWithMargins(
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ) {
        val lp = child.layoutParams as MarginLayoutParams
        val childWidthMeasureSpec = getChildMeasureSpec(
            parentWidthMeasureSpec,
            widthUsed + lp.leftMargin + lp.rightMargin,
            lp.width
        )
        val childHeightMeasureSpec = getChildMeasureSpec(
            parentHeightMeasureSpec,
            heightUsed + lp.topMargin + lp.bottomMargin,
            lp.height
        )
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }


}