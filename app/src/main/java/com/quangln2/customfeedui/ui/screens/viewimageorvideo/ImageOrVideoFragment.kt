package com.quangln2.customfeedui.ui.screens.viewimageorvideo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.quangln2.customfeedui.databinding.FragmentImageOrVideoBinding
import com.quangln2.customfeedui.others.utils.DownloadUtils


class ImageOrVideoFragment : Fragment() {
    private lateinit var binding: FragmentImageOrVideoBinding
    private lateinit var player: Player
    private var currentVideoPosition: Long = -1
    private var urlTmp = ""

    override fun onStart(){
        super.onStart()
        val listOfUrls = arguments?.getStringArrayList("listOfUrls")
        val position = arguments?.getInt("position")

        currentVideoPosition = arguments?.getLong("currentVideoPosition") ?: -1

        if (listOfUrls != null && position != null) {
            val url = if (DownloadUtils.doesLocalFileExist(
                    listOfUrls[position],
                    requireContext()
                )
            ) DownloadUtils.getTemporaryFilePath(listOfUrls[position], requireContext()) else listOfUrls[position]
            if (url.contains(".mp4")) {
                binding.fullVideoView.visibility = View.VISIBLE
                binding.fullImageView.visibility = View.GONE
                binding.fullVideoProgressBar.visibility = View.VISIBLE
                binding.fullVideoPlayButton.visibility = View.INVISIBLE

                initializeVideoForLoading(url)

                binding.fullVideoProgressBar.visibility = View.INVISIBLE
                binding.fullVideoPlayButton.visibility = View.VISIBLE

                player.addListener(
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            super.onPlaybackStateChanged(playbackState)
                            when (playbackState) {
                                Player.STATE_READY -> {
                                    binding.fullVideoPlayButton.visibility = View.INVISIBLE
                                }
                                Player.STATE_ENDED -> {
                                    binding.fullVideoPlayButton.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                )

            } else {
                binding.fullVideoView.visibility = View.GONE
                binding.fullImageView.visibility = View.VISIBLE
                Glide.with(requireContext()).load(url).listener(
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
                ) .into(binding.fullImageView)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageOrVideoBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun initializeVideoForLoading(url: String) {
        urlTmp = url

        player = ExoPlayer.Builder(requireContext()).build()
        binding.fullVideoView.player = player
        val mediaItem = MediaItem.fromUri(url)

        player.setMediaItem(mediaItem)
        player.seekTo(currentVideoPosition)
        player.playWhenReady = true
        player.prepare()
    }

    override fun onPause() {
        super.onPause()
        val mimeType = DownloadUtils.getMimeType(urlTmp)
        if(mimeType != null && mimeType.contains("video")){
            player.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        val mimeType = DownloadUtils.getMimeType(urlTmp)
        if(mimeType != null && mimeType.contains("video")){
            player.release()
        }
    }
}