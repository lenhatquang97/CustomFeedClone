package com.quangln2.customfeed.ui.customview

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.quangln2.customfeed.R
import com.quangln2.customfeed.others.utils.FileUtils.getVideoThumbnail

class VideoThumbnailView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, url: String) : this(context) {
        this.url = url
        init()
    }

    var url = ""
    lateinit var thumbnailView: ImageView
    lateinit var playButton: ImageView

    private fun init() {
        val view = LayoutInflater.from(context).inflate(R.layout.video_thumbnail_view, this, true)
        thumbnailView = view.findViewById(R.id.thumbnail_view)
        playButton = view.findViewById(R.id.play_button)
        thumbnailView.scaleType = ImageView.ScaleType.FIT_XY
        thumbnailView.setImageDrawable(getVideoThumbnail(Uri.parse(url), context, ""))

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}