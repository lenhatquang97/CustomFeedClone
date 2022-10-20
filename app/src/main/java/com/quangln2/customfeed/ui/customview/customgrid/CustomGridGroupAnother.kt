package com.quangln2.customfeed.ui.customview.customgrid

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import com.quangln2.customfeed.data.models.uimodel.RectanglePoint
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.ui.customview.VideoThumbnailView

fun getGridItemsLocation(childCount: Int, frameLayout: FrameLayout): List<RectanglePoint> {
    var itemNumber = childCount
    val contentPadding = 8

    val rectangles = when (childCount) {
        0 -> emptyList<RectanglePoint>()
        1 -> CustomGridMigration.drawOneChildGrid()
        2, 3, 4 -> {
            when (frameLayout.getChildAt(0)) {
                is ImageView, is VideoThumbnailView, is FrameLayout, is VideoView, is LoadingVideoView -> {
                    val firstItemWidth = frameLayout.getChildAt(0).measuredWidth
                    val firstItemHeight = frameLayout.getChildAt(0).measuredHeight

                    if (firstItemWidth >= firstItemHeight) {
                        CustomGridMigration.drawHorizontalGridMoreThanTwoAndLessThanFourGrid(childCount)
                    } else {
                        CustomGridMigration.drawHorizontalGridMoreThanTwoAndLessThanFourGrid(childCount)
                    }
                }
                else -> emptyList()
            }

        }
        5 -> {
            when (frameLayout.getChildAt(0)) {
                is ImageView, is VideoThumbnailView, is FrameLayout, is VideoView, is LoadingVideoView -> {
                    val firstItemWidth = frameLayout.getChildAt(0).measuredWidth
                    val firstItemHeight = frameLayout.getChildAt(0).measuredHeight
                    if (firstItemWidth >= firstItemHeight) {
                        CustomGridMigration.drawHorizontalGridWithFiveGrid()
                    } else {
                        CustomGridMigration.drawVerticalGridWithFiveGrid()
                    }
                }
                else -> {
                    emptyList()
                }
            }
        }
        6 -> CustomGridMigration.drawSixChildrenGrid()
        7 -> CustomGridMigration.drawSevenChildrenGrid()
        8 -> {
            when (frameLayout.getChildAt(0)) {
                is ImageView, is VideoThumbnailView, is FrameLayout, is VideoView, is LoadingVideoView -> {
                    val firstItemWidth = frameLayout.getChildAt(0).measuredWidth
                    val firstItemHeight = frameLayout.getChildAt(0).measuredHeight
                    if (firstItemWidth >= firstItemHeight) {
                        CustomGridMigration.drawHorizontalGridWithEightGrid()
                    } else {
                        CustomGridMigration.drawVerticalGridWithEightGrid()
                    }
                }
                else -> {
                    emptyList()
                }
            }
        }
        else -> {
            itemNumber = 9
            CustomGridMigration.drawNineChildrenGrid()
        }
    }
    return rectangles
}

fun drawCustomGridGroupAnother(context: Context): FrameLayout{
    val frameLayout = object : FrameLayout(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)

            var itemNumber = childCount
            val contentPadding = 8
            val widthGrid = width.toFloat()

            val rectangles = when (childCount) {
                0 -> emptyList<RectanglePoint>()
                1 -> CustomGridMigration.drawOneChildGrid()
                2, 3, 4 -> {
                    when (getChildAt(0)) {
                        is ImageView, is VideoThumbnailView, is FrameLayout, is VideoView, is LoadingVideoView -> {
                            val firstItemWidth = getChildAt(0).measuredWidth
                            val firstItemHeight = getChildAt(0).measuredHeight

                            if (firstItemWidth >= firstItemHeight) {
                                CustomGridMigration.drawHorizontalGridMoreThanTwoAndLessThanFourGrid(childCount)
                            } else {
                                CustomGridMigration.drawHorizontalGridMoreThanTwoAndLessThanFourGrid(childCount)
                            }
                        }
                        else -> emptyList()
                    }

                }
                5 -> {
                    when (getChildAt(0)) {
                        is ImageView, is VideoThumbnailView, is FrameLayout, is VideoView, is LoadingVideoView -> {
                            val firstItemWidth = getChildAt(0).measuredWidth
                            val firstItemHeight = getChildAt(0).measuredHeight
                            if (firstItemWidth >= firstItemHeight) {
                                CustomGridMigration.drawHorizontalGridWithFiveGrid()
                            } else {
                                CustomGridMigration.drawVerticalGridWithFiveGrid()
                            }
                        }
                        else -> {
                            emptyList()
                        }
                    }
                }
                6 -> CustomGridMigration.drawSixChildrenGrid()
                7 -> CustomGridMigration.drawSevenChildrenGrid()
                8 -> {
                    when (getChildAt(0)) {
                        is ImageView, is VideoThumbnailView, is FrameLayout, is VideoView, is LoadingVideoView -> {
                            val firstItemWidth = getChildAt(0).measuredWidth
                            val firstItemHeight = getChildAt(0).measuredHeight
                            if (firstItemWidth >= firstItemHeight) {
                                CustomGridMigration.drawHorizontalGridWithEightGrid()
                            } else {
                                CustomGridMigration.drawVerticalGridWithEightGrid()
                            }
                        }
                        else -> {
                            emptyList()
                        }
                    }
                }
                else -> {
                    itemNumber = 9
                    CustomGridMigration.drawNineChildrenGrid()
                }
            }

            for (i in 0 until itemNumber) {
                val child = getChildAt(i)
                if (child != null && child is ViewGroup) {
                    val width = (rectangles[i].rightBottom.x * widthGrid) - (rectangles[i].leftTop.x * widthGrid) + contentPadding
                    val height = (rectangles[i].rightBottom.y * widthGrid) - (rectangles[i].leftTop.y * widthGrid) + contentPadding
                    child.measure(width.toInt(), height.toInt())
                }
                child?.layout(
                    (rectangles[i].leftTop.x.toInt() * widthGrid).toInt() + contentPadding,
                    (rectangles[i].leftTop.y.toInt() * widthGrid).toInt() + contentPadding,
                    (rectangles[i].rightBottom.x.toInt() * widthGrid).toInt() - contentPadding,
                    (rectangles[i].rightBottom.y.toInt() * widthGrid).toInt() - contentPadding
                )
            }
        }
    }
    return frameLayout
}