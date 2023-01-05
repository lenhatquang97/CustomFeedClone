package com.quangln2.customfeedui.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.controllers.FeedCtrl.isMute
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.imageloader.domain.ImageLoader
import com.quangln2.customfeedui.videocache.VideoCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job


class LoadingVideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    lateinit var progressBar: ProgressBar
    lateinit var playButton: ImageView
    lateinit var playerView: PlayerView
    lateinit var crossButton: ImageView
    lateinit var thumbnailView: ImageView
    private var currentPosition = 0L

    private var webUrl = ""
    private var fileUri: Uri? = null
    private lateinit var soundButton: ImageView

    //Use for load thumbnail
    constructor(context: Context, uri: Uri): this(context){
        this.fileUri = uri
        initFindById()
        initForShowThumbnail()
    }

    constructor(context: Context, webUrl: String) : this(context) {
        this.webUrl = webUrl
        initFindById()
    }

    private fun initFindById(){
        val view = LayoutInflater.from(context).inflate(R.layout.loading_video_view, this, true)
        progressBar = view.findViewById(R.id.my_spinner)
        playButton = view.findViewById(R.id.play_button)
        soundButton = view.findViewById(R.id.sound_button)
        playerView = view.findViewById(R.id.player_view)
        crossButton = view.findViewById(R.id.cross_x)
        thumbnailView = view.findViewById(R.id.thumbnail_view)
    }

     private fun initForShowThumbnail() {
        progressBar.visibility = View.GONE
        val imageLoader = ImageLoader(context,0,0, CoroutineScope(Job()))
        if(fileUri == null){
            imageLoader.loadImage(webUrl,thumbnailView, BitmapCustomParams())
        } else {
            imageLoader.loadImage(fileUri.toString(),thumbnailView, BitmapCustomParams())
        }
    }

     fun initForShowThumbnail(width: Int, height: Int) {
        progressBar.visibility = View.GONE
        val imageLoader = ImageLoader(context,width,height, CoroutineScope(Job()))
        if(fileUri == null){
            imageLoader.loadImage(webUrl,thumbnailView, BitmapCustomParams())
        } else {
            imageLoader.loadImage(fileUri.toString(),thumbnailView, BitmapCustomParams())
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun init(player: ExoPlayer) {
        playerView.player = player
        playButton.visibility = View.VISIBLE
        playerView.visibility = View.INVISIBLE

        //Set icon whether mute or not
        if (isMute) soundButton.setImageDrawable(context.getDrawable(R.drawable.volume_off))
        else soundButton.setImageDrawable(context.getDrawable(R.drawable.volume_on))

        //Button for changing sound
        soundButton.setOnClickListener {
            isMute = !isMute
            if (isMute) {
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
        val mediaItem = MediaItem.fromUri(if(fileUri == null) webUrl else fileUri.toString())
        val mediaSource = ProgressiveMediaSource.Factory(VideoCache.buildCacheDataSourceFactory(context)).createMediaSource(mediaItem)
        player.setMediaSource(mediaSource)
        player.prepare()
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        playerView.layoutParams.width = width
        playerView.layoutParams.height = height

        thumbnailView.layoutParams.width = width
        thumbnailView.layoutParams.height = height
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun playVideo(player: ExoPlayer) {
        init(player)

        player.seekTo(currentPosition)
        playerView.visibility = View.VISIBLE
        playButton.visibility = View.INVISIBLE
        thumbnailView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        soundButton.visibility = View.VISIBLE

        player.volume = if(isMute) 0f else 1f
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

    fun onEndPlayVideo(player: ExoPlayer){
        player.seekTo(0)
        player.pause()
        thumbnailView.visibility = View.VISIBLE
        playButton.visibility = View.VISIBLE
        currentPosition = 0
        playerView.player = null

    }
}