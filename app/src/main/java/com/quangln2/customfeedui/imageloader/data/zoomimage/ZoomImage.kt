package com.quangln2.customfeedui.imageloader.data.zoomimage

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.ceil
import kotlin.math.sqrt


enum class ZoomMode {
    NONE,
    DRAG,
    ZOOM
}
class ZoomImage: AppCompatImageView, View.OnTouchListener {
    constructor(context: Context) : super(context)
    @SuppressLint("ClickableViewAccessibility")
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        this.setOnTouchListener(this)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    //Prevent doing outside ImageView
    private var isOutSide = false
    //Zoom mode
    private var mode = ZoomMode.NONE
    //Get old distance so as to calculate zoom proportion
    private var oldDist = 1f

    private var myMatrix: Matrix = Matrix()
    private var savedMatrix: Matrix = Matrix()
    private val start = PointF()
    private val mid = PointF()
    private var scale = 1f


    private var centerLocation: Pair<Float, Float> = Pair(0f, 0f)

    private fun distanceBetweenTwoPoints(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toInt().toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        makeCenterImage()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(myMatrix)
                start.set(event.x, event.y)
                mode = ZoomMode.DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = distanceBetweenTwoPoints(event)
                if (oldDist > 5f) {
                    savedMatrix.set(myMatrix)
                    midPoint(mid, event)
                    mode = ZoomMode.ZOOM
                }
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                mode = ZoomMode.NONE
            }
            MotionEvent.ACTION_OUTSIDE -> {
                mode = ZoomMode.NONE
                isOutSide = true
            }
            MotionEvent.ACTION_MOVE -> if (!isOutSide) {
                when(mode){
                    ZoomMode.DRAG -> {
                        if(event.pointerCount == 1) {
                            doDragFunction(event)
                        }
                    }
                    else -> {
                        if(event.pointerCount == 2){
                            doZoomFunction(event)
                        }
                    }
                }
            }
        }
        imageMatrix = myMatrix
        if(scale > 1f) parent.requestDisallowInterceptTouchEvent(true)
        else parent.requestDisallowInterceptTouchEvent(false)

        return true
    }

    private fun makeCenterImage(){
        if(drawable != null) {
            val statusBarHeight = ceil((25 * context.resources.displayMetrics.density).toDouble()).toInt()
            val phoneHeight = Resources.getSystem().displayMetrics.heightPixels - statusBarHeight
            val imgHeight = drawable.intrinsicHeight
            val moveDy = (phoneHeight - imgHeight) / 2f
            myMatrix.setTranslate(0f,  moveDy)
            imageMatrix = myMatrix
            val points = FloatArray(8)
            myMatrix.mapPoints(points)
            centerLocation = Pair(points[1], points[5] + phoneHeight + statusBarHeight)
        }
    }
    private fun doDragFunction(event: MotionEvent){
        myMatrix.set(savedMatrix)
        val dx = event.x - start.x
        val dy = event.y - start.y
        myMatrix.postTranslate(dx, dy)
        checkConstraintInDragFunction()
    }

    private fun doZoomFunction(event: MotionEvent){
        try{
            val newDist = distanceBetweenTwoPoints(event)
            if(newDist > 5f){
                myMatrix.set(savedMatrix)
                val oldMatrix = Matrix().apply {
                    set(myMatrix)
                    scale = newDist / oldDist
                    postScale(scale, scale, mid.x, mid.y)
                }
                checkConstraintInZoomFunction(oldMatrix)
            }
        } catch(e: Exception){
            Log.d("Exception", e.cause.toString())
        }
    }

    private fun checkConstraintInDragFunction(){
        val points = FloatArray(8)
        myMatrix.mapPoints(points)
        val firstCorner = Pair(points[0], points[1])
        val secondCorner = Pair(points[2] + width, points[3])
        val thirdCorner = Pair(points[4] + width, points[5] + height)
        val phoneWidth = Resources.getSystem().displayMetrics.widthPixels
        val isOutOfBoundWidth = (firstCorner.first < 0 && secondCorner.first < phoneWidth) || (firstCorner.first > 0 && secondCorner.first > phoneWidth)
        val isOutOfBoundHeight = (firstCorner.second < centerLocation.first && thirdCorner.second < centerLocation.second) || (firstCorner.second > centerLocation.first && thirdCorner.second > centerLocation.second)
        if(isOutOfBoundWidth && scale == 1f) myMatrix.postTranslate(-firstCorner.first, 0f)
        if(isOutOfBoundHeight && scale == 1f) myMatrix.postTranslate(0f, -(firstCorner.second - centerLocation.first))
    }

    private fun checkConstraintInZoomFunction(oldMatrix: Matrix){
        val points = FloatArray(8)
        oldMatrix.mapPoints(points)
        val firstCorner = Pair(points[0], points[1])
        if(firstCorner.first >= 0f && firstCorner.second >= centerLocation.first){
            myMatrix.set(matrix)
            makeCenterImage()
        } else {
            myMatrix.set(oldMatrix)
        }

    }
}