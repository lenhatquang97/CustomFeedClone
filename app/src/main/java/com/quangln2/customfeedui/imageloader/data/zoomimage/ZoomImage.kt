package com.quangln2.customfeedui.imageloader.data.zoomimage

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
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
                    ZoomMode.DRAG -> doDragFunction(event)
                    else -> doZoomFunction(event)
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
        }
    }
    private fun doDragFunction(event: MotionEvent){
        myMatrix.set(savedMatrix)
        val dx = event.x - start.x
        val dy = event.y - start.y
        myMatrix.postTranslate(dx, dy)
        checkConstraintInDragFunction(dx, dy)
    }

    private fun doZoomFunction(event: MotionEvent){
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
    }

    private fun checkConstraintInDragFunction(dx: Float, dy: Float){
        val rect = RectF()
        myMatrix.mapRect(rect)
        val phoneWidth = Resources.getSystem().displayMetrics.widthPixels
        val notAllowDragIllegally = rect.right + width <= phoneWidth && rect.left >= 0
        Log.d("ConstraintDrag","${rect.left} ${rect.right + width} $notAllowDragIllegally")
        if(!notAllowDragIllegally) myMatrix.postTranslate(-dx, -dy)
    }

    private fun checkConstraintInZoomFunction(oldMatrix: Matrix){
        val rect = RectF()
        oldMatrix.mapRect(rect)
        val zoomIllegally = rect.left > 0
        Log.d("ConstraintZoom", "${rect.left} ${rect.right + width} $zoomIllegally")
        if(zoomIllegally) scale = 1f else myMatrix.set(oldMatrix)
    }
}