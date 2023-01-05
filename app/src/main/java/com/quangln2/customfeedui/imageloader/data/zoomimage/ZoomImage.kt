package com.quangln2.customfeedui.imageloader.data.zoomimage

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.quangln2.customfeedui.data.constants.ConstantSetup
import kotlin.math.ceil
import kotlin.math.sqrt

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
        checkConstraintInDragFunction(dx, dy)
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
    private fun createPairArray(it: Drawable): Pair<Pair<Float, Float>, Pair<Float, Float>>{
        val values = FloatArray(9)
        myMatrix.getValues(values)
        val leftTop = Pair(values[2], values[5])
        val rightBottom = Pair(values[2] + it.intrinsicWidth * values[0], values[5] + it.intrinsicHeight * values[4])
        return Pair(leftTop, rightBottom)
    }

    private fun checkConstraintInDragFunction(dx: Float, dy: Float){
        drawable?.let {
            // values[2] and values[5] are the x,y coordinates of the top left corner of the drawable image, regardless of the zoom factor.
            // values[0] and values[4] are the zoom factors for the image's width and height respectively. If you zoom at the same factor, these should both be the same value.
            var (leftTop, rightBottom) = createPairArray(it)
            Log.d("ZoomImage","2 main points: $leftTop $rightBottom")
            if(leftTop.first < 0 && rightBottom.first > ConstantSetup.PHONE_WIDTH){
                myMatrix.postTranslate(dx, dy)
                val result = createPairArray(it)
                leftTop = result.first
                rightBottom = result.second
            }
            val outOfBoundXCondition = (leftTop.first > 0 && rightBottom.first > ConstantSetup.PHONE_WIDTH) || (leftTop.first < 0 && rightBottom.first < ConstantSetup.PHONE_WIDTH)
            if(outOfBoundXCondition){
                myMatrix.postTranslate(-dx, 0f)
                val result = createPairArray(it)
                leftTop = result.first
                rightBottom = result.second
            }
            val outOfBoundYCondition = (leftTop.second > 0 && rightBottom.second > ConstantSetup.PHONE_HEIGHT) || (leftTop.second < 0 && rightBottom.second < ConstantSetup.PHONE_HEIGHT)
            if(outOfBoundYCondition) myMatrix.postTranslate(0f, -dy)
        }
    }

    private fun checkConstraintInZoomFunction(oldMatrix: Matrix){
        //TODO: Check constraint in zoom function
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