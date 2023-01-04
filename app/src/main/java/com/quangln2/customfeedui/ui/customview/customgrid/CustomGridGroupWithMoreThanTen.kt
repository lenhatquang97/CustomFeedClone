package com.quangln2.customfeedui.ui.customview.customgrid


import com.quangln2.customfeedui.data.models.uimodel.RectanglePoint

fun getGridItemsLocationWithMoreThanTen(childCount: Int, firstItemWidth: Int = 0, firstItemHeight: Int = 0): List<RectanglePoint> {
    val rectangles = when (childCount) {
        0 -> emptyList()
        1 -> CustomGridMigration.drawOneChildGrid()
        2, 3, 4 -> {
            if (firstItemWidth >= firstItemHeight)
                CustomGridMigration.drawHorizontalGridMoreThanTwoAndLessThanFourGrid(childCount)
            else
                CustomGridMigration.drawVerticalGridMoreThanTwoAndLessThanFourGrid(childCount)

        }
        5 -> {
            if (firstItemWidth >= firstItemHeight)
                CustomGridMigration.drawHorizontalGridWithFiveGrid()
            else
                CustomGridMigration.drawVerticalGridWithFiveGrid()

        }
        6 -> CustomGridMigration.drawSixChildrenGrid()
        7 -> CustomGridMigration.drawSevenChildrenGrid()
        8 -> {
            if (firstItemWidth >= firstItemHeight)
                CustomGridMigration.drawHorizontalGridWithEightGrid()
            else
                CustomGridMigration.drawVerticalGridWithEightGrid()
        }
        else -> CustomGridMigration.drawMoreThanTenChildrenGrid(childCount)

    }
    return rectangles
}