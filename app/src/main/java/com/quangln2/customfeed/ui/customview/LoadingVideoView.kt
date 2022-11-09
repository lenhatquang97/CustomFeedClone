package com.quangln2.customfeed.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.controllers.FeedCtrl.isMute


class LoadingVideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    lateinit var progressBar: ProgressBar
    lateinit var playButton: ImageView
    private lateinit var soundButton: ImageView
    lateinit var playerView: PlayerView
    lateinit var crossButton: ImageView
    lateinit var thumbnailView: ImageView

    private var url = ""
    private val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).format(DecodeFormat.PREFER_RGB_565)
    var currentPosition = 0L

    constructor(context: Context, url: String) : this(context) {
        this.url = url
        initFindById()
        initForShowThumbnail()
    }
    private fun initFindById(){
        val view = LayoutInflater.from(context).inflate(R.layout.loading_video_view, this, true)
        progressBar = view.findViewById(R.id.my_spinner)
        playButton = view.findViewById(R.id.play_button)
        soundButton = view.findViewById(R.id.sound_button)
        playerView = view.findViewById(R.id.player_view)
        crossButton = view.findViewById(R.id.cross_x)
        thumbnailView = view.findViewById(R.id.thumbnail_view)

        soundButton.setOnClickListener {
            val currentState = isMute.value ?: false
            isMute.value = !currentState
        }
    }


    private fun initForShowThumbnail() {
        Glide.with(context).load(url).apply(requestOptions).placeholder(ColorDrawable(Color.parseColor("#aaaaaa"))).into(object : SimpleTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                progressBar.visibility = View.GONE
                thumbnailView.setImageDrawable(resource)
            }
        })
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun init(player: ExoPlayer) {
        playerView.player = player
        playButton.visibility = View.VISIBLE
        playerView.visibility = View.INVISIBLE

        isMute.observeForever {
            if (it) {
                soundButton.setImageDrawable(context.getDrawable(R.drawable.volume_off))
                player.volume = 0f
            } else {
                soundButton.setImageDrawable(context.getDrawable(R.drawable.volume_on))
                player.volume = 1f
            }
        }

        prepare(player)
    }

    private fun prepare(player: ExoPlayer) {
        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            thumbnailView.visibility = View.VISIBLE
                            playButton.visibility = View.VISIBLE
                            currentPosition = player.currentPosition
                            playerView.player = null
                        }
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

    fun playVideo(player: ExoPlayer) {
        init(player)

        player.seekTo(currentPosition)
        playerView.visibility = View.VISIBLE
        playButton.visibility = View.INVISIBLE
        thumbnailView.visibility = View.GONE
        progressBar.visibility = View.GONE

        val isMuted = isMute.value
        player.volume = if(isMuted == true) 0f else 1f
        player.play()
    }

    fun pauseAndReleaseVideo(player: ExoPlayer){
        pauseVideo(player)
    }

    private fun pauseVideo(player: ExoPlayer) {
        playerView.visibility = View.INVISIBLE
        thumbnailView.visibility = View.VISIBLE
        playButton.visibility = View.VISIBLE
        currentPosition = player.currentPosition
        player.pause()
        player.seekTo(0)
        playerView.player = null
    }
}