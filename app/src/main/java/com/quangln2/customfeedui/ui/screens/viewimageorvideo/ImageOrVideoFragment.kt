package com.quangln2.customfeedui.ui.screens.viewimageorvideo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.quangln2.customfeedui.databinding.FragmentImageOrVideoBinding
import com.quangln2.customfeedui.others.utils.DownloadUtils


class ImageOrVideoFragment(private val player: ExoPlayer) : Fragment() {
    private lateinit var binding: FragmentImageOrVideoBinding
    private var currentVideoPosition = -1L
    private var urlTmp = ""

    override fun onStart(){
        super.onStart()
        val listOfUrls = arguments?.getStringArrayList("listOfUrls")
        val position = arguments?.getInt("position")

        currentVideoPosition = arguments?.getLong("currentVideoPosition") ?: -1

        if (listOfUrls != null && position != null) {
            val doesLocalFileExist = DownloadUtils.doesLocalFileExist(listOfUrls[position], requireContext())
            urlTmp = if (doesLocalFileExist) {
                DownloadUtils.getTemporaryFilePath(listOfUrls[position], requireContext())
            } else {
                listOfUrls[position]
            }

            loadImageThumbnail()

            val mimeType = DownloadUtils.getMimeType(urlTmp)
            mimeType?.apply {
                if (this.contains("video")) {
                    binding.fullVideoView.visibility = View.VISIBLE
                    binding.fullImageView.visibility = View.INVISIBLE
                    binding.fullVideoPlayButton.visibility = View.INVISIBLE
                } else {
                    binding.fullVideoView.visibility = View.INVISIBLE
                    binding.fullImageView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageOrVideoBinding.inflate(inflater, container, false)
        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            binding.fullVideoPlayButton.visibility = View.VISIBLE
                            player.seekTo(0)
                        }
                    }
                }
            }
        )
        return binding.root
    }

    private fun loadImageThumbnail(){
        Glide.with(requireContext()).load(urlTmp).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).format(DecodeFormat.PREFER_RGB_565)).listener(
            object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.fullVideoProgressBar.visibility = View.GONE
                    return false
                }
            }
        ).centerInside().into(binding.fullImageView)
    }

    private fun initializeVideoForLoading(url: String) {
        binding.fullVideoView.player = player
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.seekTo(currentVideoPosition)
        player.prepare()
        player.play()
        binding.fullImageView.visibility = View.INVISIBLE
        binding.fullVideoPlayButton.visibility = View.INVISIBLE
    }

    override fun onPause() {
        super.onPause()
        val mimeType = DownloadUtils.getMimeType(urlTmp)
        mimeType?.apply {
            if (this.contains("video")) {
                player.pause()
                currentVideoPosition = player.currentPosition

                binding.fullVideoView.player = null
                binding.fullImageView.visibility = View.VISIBLE
                binding.fullVideoPlayButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val mimeType = DownloadUtils.getMimeType(urlTmp)
        mimeType?.apply {
            if(this.contains("video")){
                initializeVideoForLoading(urlTmp)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}