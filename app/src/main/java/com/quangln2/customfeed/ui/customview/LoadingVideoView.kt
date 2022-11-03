package com.quangln2.customfeed.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.quangln2.customfeed.R


class LoadingVideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    lateinit var progressBar: ProgressBar
    lateinit var playButton: ImageView
    lateinit var soundButton: ImageView
    lateinit var playerView: PlayerView
    lateinit var crossButton: ImageView
    lateinit var player: ExoPlayer
    lateinit var thumbnailView: ImageView

    private var url = ""
    private val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).format(DecodeFormat.PREFER_RGB_565)
    private var isMute = false
    private var isReleased = false
    private var currentPosition = 0L


    constructor(context: Context, url: String, player: ExoPlayer) : this(context) {
        this.url = url
        this.player = player
        init()
    }

    constructor(context: Context, url: String) : this(context) {
        this.url = url
        initForShowThumbnail()
    }

    private fun initForShowThumbnail() {
        val view = LayoutInflater.from(context).inflate(R.layout.loading_video_view, this, true)
        progressBar = view.findViewById(R.id.my_spinner)
        playButton = view.findViewById(R.id.play_button)
        soundButton = view.findViewById(R.id.sound_button)
        playerView = view.findViewById(R.id.player_view)
        crossButton = view.findViewById(R.id.cross_x)
        thumbnailView = view.findViewById(R.id.thumbnail_view)

        thumbnailView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        Glide.with(context).load(url).apply(requestOptions).into(thumbnailView)
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun init() {

        val view = LayoutInflater.from(context).inflate(R.layout.loading_video_view, this, true)
        progressBar = view.findViewById(R.id.my_spinner)
        playButton = view.findViewById(R.id.play_button)
        soundButton = view.findViewById(R.id.sound_button)
        playerView = view.findViewById(R.id.player_view)
        crossButton = view.findViewById(R.id.cross_x)
        thumbnailView = view.findViewById(R.id.thumbnail_view)


        soundButton.setOnClickListener {
            if (isMute) {
                soundButton.setImageDrawable(context.getDrawable(R.drawable.volume_on))
                player.volume = 1f
                isMute = false
            } else {
                soundButton.setImageDrawable(context.getDrawable(R.drawable.volume_off))
                player.volume = 0f
                isMute = true

            }
        }

        progressBar.visibility = View.VISIBLE
        playButton.visibility = View.VISIBLE
        playerView.visibility = View.INVISIBLE
        thumbnailView.visibility = View.VISIBLE

        Glide.with(context).load(url).apply(requestOptions).into(thumbnailView)

        prepare()
    }

    private fun prepare() {
        playerView.player = player

        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_READY) {
                        progressBar.visibility = View.INVISIBLE
                    } else if (playbackState == Player.STATE_IDLE) {
                        playButton.visibility = View.VISIBLE
                    }
                }

            }
        )
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(context).build()
        playerView.player = player

        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_READY) {
                        progressBar.visibility = View.INVISIBLE
                    } else if (playbackState == Player.STATE_IDLE) {
                        playButton.visibility = View.VISIBLE
                    }
                }

            }
        )

        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        playerView.layoutParams.width = width
        playerView.layoutParams.height = height
        setMeasuredDimension(width, height)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun playVideo() {
        playerView.visibility = View.VISIBLE
        thumbnailView.visibility = View.INVISIBLE

        if (isReleased) {
            isReleased = false
            initPlayer()
            player.seekTo(currentPosition)
        }
        progressBar.visibility = View.GONE
        playButton.visibility = View.INVISIBLE
        player.play()
    }

    fun pauseAndReleaseVideo(){
        pauseVideo()
        releaseVideo()
    }

    fun pauseVideo() {
        playerView.visibility = View.INVISIBLE
        thumbnailView.visibility = View.VISIBLE
        playButton.visibility = View.VISIBLE
        player.pause()

        currentPosition = player.currentPosition
    }

    fun releaseVideo() {
        isReleased = true
        player.release()
    }

}