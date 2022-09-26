package com.quangln2.customfeed.customview

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
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
import kotlinx.coroutines.withContext


class LoadingVideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {
    lateinit var videoView: SurfaceView
    lateinit var progressBar: ProgressBar
    lateinit var playButton: ImageView
    lateinit var soundButton: ImageView
    var mediaPlayer: MediaPlayer? = null
    lateinit var surfaceHolder: SurfaceHolder
    var url = ""

    constructor(context: Context, url: String) : this(context) {
        this.url = url
    }

    init {
        init()
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun init() {
        val view = LayoutInflater.from(context).inflate(R.layout.loading_video_view, this, true)
        videoView = view.findViewById(R.id.my_video_view)
        progressBar = view.findViewById(R.id.my_spinner)
        playButton = view.findViewById(R.id.play_button)
        soundButton = view.findViewById(R.id.sound_button)


        //soundButton onClickListener
        soundButton.setOnClickListener {
            val drawable1 = soundButton.drawable.constantState
            val drawable2 = context.getDrawable(R.drawable.volume_off)?.constantState
            if (drawable1 != null) {
                if(drawable1 == drawable2){
                    soundButton.setImageDrawable(context.getDrawable(R.drawable.volume_on))
                    mediaPlayer?.setVolume(1f,1f)
                } else {
                    soundButton.setImageDrawable(context.getDrawable(R.drawable.volume_off))
                    mediaPlayer?.setVolume(0f,0f)
                }
            }
        }

        //Visibility
        progressBar.visibility = View.VISIBLE
        playButton.visibility = View.INVISIBLE

        //Set surfaceHolder
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
            mediaPlayer?.reset()
            mediaPlayer?.setDisplay(surfaceHolder)
            try {
                mediaPlayer?.setOnBufferingUpdateListener { mp, percent ->
                    if (percent >= 1 && mp?.isPlaying == false) {
                        playButton.visibility = View.VISIBLE
                        progressBar.visibility = View.INVISIBLE
                    }
                }
                mediaPlayer?.setDataSource(url)
                mediaPlayer?.setOnPreparedListener(this@LoadingVideoView)
                mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer?.prepareAsync()



            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }
    override fun onPrepared(mp: MediaPlayer?) {
        try{
            mediaPlayer?.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

        }catch (e: Exception){
            e.printStackTrace()
            Log.d("TAG", "onPrepared: ${e.cause}")
        }
    }


}