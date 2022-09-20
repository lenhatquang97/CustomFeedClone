package com.quangln2.customfeed.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.quangln2.customfeed.R

class CustomLayer : View {
    var textValue = "+1"

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        setBackgroundColor(resources.getColor(R.color.black))
        canvas?.drawText(textValue, (width / 2 - 15).toFloat(), (height / 2).toFloat(), Paint().apply {
            textSize = 30f
            color = resources.getColor(R.color.white)
        })
        super.onDraw(canvas)
    }
}