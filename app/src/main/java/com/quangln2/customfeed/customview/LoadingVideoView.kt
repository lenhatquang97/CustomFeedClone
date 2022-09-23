package com.quangln2.customfeed.customview

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.quangln2.customfeed.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoadingVideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {
    lateinit var videoView: SurfaceView
    lateinit var progressBar: ProgressBar
    lateinit var playButton: ImageView
    var mediaPlayer: MediaPlayer? = null
    lateinit var surfaceHolder: SurfaceHolder
    var url = ""

    constructor(context: Context, url: String) : this(context) {
        this.url = url
    }

    init {
        init()
    }


    private fun init() {
        val view = LayoutInflater.from(context).inflate(R.layout.loading_video_view, this, true)
        videoView = view.findViewById(R.id.my_video_view)
        progressBar = view.findViewById(R.id.my_spinner)
        playButton = view.findViewById(R.id.play_button)

        progressBar.visibility = View.INVISIBLE
        playButton.visibility = View.INVISIBLE

        surfaceHolder = videoView.holder
        surfaceHolder.addCallback(this)

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        videoView.layoutParams.width = widthMeasureSpec
        videoView.layoutParams.height = heightMeasureSpec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        CoroutineScope(Dispatchers.IO).launch {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDisplay(surfaceHolder)
            try {
                mediaPlayer?.setDataSource(url)
                mediaPlayer?.prepare()
                mediaPlayer?.setOnPreparedListener(this@LoadingVideoView)
                mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}
    override fun onPrepared(mp: MediaPlayer?) {
        mediaPlayer?.start()
    }


}