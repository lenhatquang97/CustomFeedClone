package com.quangln2.customfeed.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.net.toUri
import com.quangln2.customfeed.R

class CustomImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    lateinit var imgView: ImageView
    lateinit var crossButton: ImageView

    var url = ""

    constructor(context: Context, url: String) : this(context) {
        this.url = url
        init()
    }

    private fun init() {
        imgView = generateImageView()
        crossButton = generateCrossButton()
        addView(imgView)
        addView(crossButton)
    }

    private fun generateCrossButton(): ImageView{
        val crossButton = ImageView(context)
        crossButton.setImageDrawable(context.getDrawable(R.drawable.remove_icon))
        crossButton.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        crossButton.setPadding(16, 16, 16, 16)
        return crossButton
    }
    private fun generateImageView(): ImageView{
        val imageView = ImageView(context)
        imageView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(url.toUri())
        return imageView
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChild(imgView, widthMeasureSpec, heightMeasureSpec)
        measureChild(crossButton, widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.makeMeasureSpec(imgView.measuredWidth + paddingLeft + paddingRight, MeasureSpec.getMode(widthMeasureSpec))
        val height = MeasureSpec.makeMeasureSpec(imgView.measuredHeight + paddingBottom + paddingTop, MeasureSpec.getMode(heightMeasureSpec))
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        imgView.layout(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
        crossButton.layout(width - paddingRight - crossButton.measuredWidth, paddingTop, width - paddingRight, paddingTop + crossButton.measuredHeight)
    }
}