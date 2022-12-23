package com.quangln2.customfeedui.imageloader.data.zoomimage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
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

//Reference: https://stackoverflow.com/questions/6650398/android-imageview-zoom-in-and-zoom-out
class ZoomImage: AppCompatImageView {
    constructor(context: Context) : super(context)
    @SuppressLint("ClickableViewAccessibility")
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        this.setOnTouchListener { v, event ->
            v.bringToFront()
            if(v is ZoomImage){
                v.viewZoomTransformation(v, event)
            }
            return@setOnTouchListener true
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    //Prevent doing outside ImageView
    private var isOutSide = false

    //Zoom mode
    private var mode = ZoomMode.NONE

    private val start = PointF()
    private var oldDist = 1f
    private var xCoOrdinate = 0f
    private var yCoOrdinate = 0f

    private val currentRect = Rect()

    fun viewZoomTransformation(view: View, event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                xCoOrdinate = view.x - event.rawX
                yCoOrdinate = view.y - event.rawY
                start.set(event.x, event.y)
                isOutSide = false
                mode = ZoomMode.DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = distanceBetweenTwoPoints(event)
                mode = if(oldDist > 5f) ZoomMode.ZOOM else mode
            }
            MotionEvent.ACTION_POINTER_UP -> mode = ZoomMode.NONE
            MotionEvent.ACTION_OUTSIDE -> {
                isOutSide = true
                mode = ZoomMode.NONE
            }
            MotionEvent.ACTION_MOVE -> if (!isOutSide) {
                if (mode == ZoomMode.DRAG) {
                    //Move image location for zooming
                    view.animate().x(event.rawX + xCoOrdinate).y(event.rawY + yCoOrdinate).setDuration(0).start()
                }
                if (mode == ZoomMode.ZOOM && event.pointerCount == 2) {
                    val newDist = distanceBetweenTwoPoints(event)
                    if (newDist > 5f) {
                        val scale: Float = newDist / oldDist * view.scaleX
                        //To make image which has 1x scale can fit into view.
                        view.animate().x(0f).y(0f).start()

                        //Scale image
                        view.scaleX = if(scale < 1f) 1f else minOf(scale, MAX_SCALE)
                        view.scaleY = if(scale < 1f) 1f else minOf(scale, MAX_SCALE)
                    }
                }
            }
        }
    }

    private fun distanceBetweenTwoPoints(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toInt().toFloat()
    }

}