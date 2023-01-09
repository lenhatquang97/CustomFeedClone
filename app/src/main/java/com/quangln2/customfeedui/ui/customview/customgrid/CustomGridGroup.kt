package com.quangln2.customfeedui.ui.customview.customgrid

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.models.uimodel.ItemLocation
import com.quangln2.customfeedui.ui.customview.CustomLayer
import com.quangln2.customfeedui.ui.customview.LoadingVideoView

class CustomGridGroup: ViewGroup {
    var firstItemWidth = 0
    var firstItemHeight = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    companion object{
         fun initializeDataForShowingGrid(count: Int, firstItemWidth: Int, firstItemHeight: Int): List<ItemLocation>{
            val rectangles = getGridItemsLocation(count, firstItemWidth, firstItemHeight)
            val contentPadding = 16
            val marginHorizontalSum = 16 + 32
            val widthGrid = ConstantSetup.PHONE_WIDTH - marginHorizontalSum
            val itemLocations = mutableListOf<ItemLocation>()
            for(i in rectangles.indices){
                val leftView = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                val topView = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
                val widthView = (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding
                val heightView = (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
                val itemLocation = ItemLocation(leftView, topView, widthView, heightView)
                itemLocations.add(itemLocation)
            }
            return itemLocations
        }
    }


    private fun initializeDataForShowingGrid(): List<ItemLocation>{
        val rectangles = getGridItemsLocation(this.childCount, firstItemWidth, firstItemHeight)
        val contentPadding = 16
        val marginHorizontalSum = 16 + 32
        val widthGrid = ConstantSetup.PHONE_WIDTH - marginHorizontalSum
        val itemLocations = mutableListOf<ItemLocation>()
        for(i in rectangles.indices){
            val leftView = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
            val topView = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
            val widthView = (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding
            val heightView = (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
            val itemLocation = ItemLocation(leftView, topView, widthView, heightView)
            itemLocations.add(itemLocation)
        }
        return itemLocations
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val rectangles = initializeDataForShowingGrid()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if(child is LoadingVideoView || child is CustomLayer){
                child.measure(rectangles[i].width, rectangles[i].height)
            }
            child.layout(rectangles[i].left, rectangles[i].top,
                rectangles[i].left + rectangles[i].width,
                rectangles[i].top + rectangles[i].height)
        }
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width,width)
    }
}