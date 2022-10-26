package com.quangln2.customfeed.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.quangln2.customfeed.R

class CustomLayer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    lateinit var addedImagesText: TextView

    init {
        init()
    }

    private fun init() {
        val view = View.inflate(context, R.layout.custom_layer, this)
        addedImagesText = view.findViewById(R.id.addedImagesText)
        addedImagesText.setTextColor(resources.getColor(R.color.white))
        view.setBackgroundColor(resources.getColor(R.color.black))
    }
}