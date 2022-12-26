package com.quangln2.customfeedui.imageloader.data.zoomimage

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.sqrt
enum class ZoomMode {
    NONE,
    DRAG,
    ZOOM
}
const val MAX_SCALE = 5f
class ZoomImage: AppCompatImageView {
    constructor(context: Context) : super(context)
    @SuppressLint("ClickableViewAccessibility")
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        this.setOnTouchListener { v, event ->
            v.bringToFront()
            if(v is ZoomImage){
                v.zoomTransformation(v, event)
            }
            if(scale > 1f){
                parent.requestDisallowInterceptTouchEvent(true)
            } else {
                parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener true
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    //Prevent doing outside ImageView
    private var isOutSide = false

    //Zoom mode
    private var mode = ZoomMode.NONE

    //Get old distance so as to calculate zoom proportion
    private var oldDist = 1f

    //Make a milestone as origin
    private var xMilestone = 0f
    private var yMilestone = 0f
    private var scale = 1f

    fun zoomTransformation(view: View, event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            //One finger
            MotionEvent.ACTION_DOWN -> {
                xMilestone = view.x * 1.1f - event.rawX
                yMilestone = view.y * 1.1f - event.rawY
                isOutSide = false
                mode = ZoomMode.DRAG
            }
            //Two finger
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = distanceBetweenTwoPoints(event)
                mode = if(oldDist > 5f) ZoomMode.ZOOM else mode
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> mode = ZoomMode.NONE
            MotionEvent.ACTION_OUTSIDE -> {
                isOutSide = true
                mode = ZoomMode.NONE
            }
            MotionEvent.ACTION_MOVE -> if (!isOutSide) {
                //Move image location for zooming
                if (mode == ZoomMode.DRAG) dragFunction(view, event)
                if (mode == ZoomMode.ZOOM && event.pointerCount == 2) zoomFunction(view, event)
            }
        }
    }
    private fun dragFunction(view: View, event: MotionEvent){
        view.animate().x(event.rawX + xMilestone).y(event.rawY + yMilestone).setDuration(0).start()
    }
    private fun zoomFunction(view: View, event: MotionEvent){
        val newDist = distanceBetweenTwoPoints(event)
        if (newDist > 5f) {
            //To make image which has 1x scale can fit into view.
            view.animate().x(0f).y(0f).start()
            val calculatedScale = newDist / oldDist * view.scaleX
            scale = if(calculatedScale < 1f) 1f else minOf(calculatedScale, MAX_SCALE)
            view.scaleX = scale
            view.scaleY = scale
        }
    }
    private fun distanceBetweenTwoPoints(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toInt().toFloat()
    }
}