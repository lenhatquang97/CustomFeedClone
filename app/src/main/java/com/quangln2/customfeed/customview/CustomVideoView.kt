package com.quangln2.customfeed.customview

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

class CustomVideoView @kotlin.jvm.JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : VideoView(context, attrs, defStyleAttr) {
    var leftAdjustment = 0
    var topAdjustment = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val videoWidth = measuredWidth
        val videoHeight = measuredHeight
        val viewWidth = getDefaultSize(0, widthMeasureSpec)
        val viewHeight = getDefaultSize(0, heightMeasureSpec)
        leftAdjustment = 0
        topAdjustment = 0
        if (videoWidth == viewWidth) {
            val newWidth = (videoWidth.toFloat() / videoHeight * viewHeight).toInt()
            setMeasuredDimension(newWidth, viewHeight)
            leftAdjustment = -(newWidth - viewWidth) / 2
        } else {
            val newHeight = (videoHeight.toFloat() / videoWidth * viewWidth).toInt()
            setMeasuredDimension(viewWidth, newHeight)
            topAdjustment = -(newHeight - viewHeight) / 2
        }
        println("videoWidth: $videoWidth videoHeight: $videoHeight viewWidth: $viewWidth viewHeight: $viewHeight")

    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        super.layout(l + leftAdjustment, t + topAdjustment, r + leftAdjustment, b + topAdjustment)
    }

}