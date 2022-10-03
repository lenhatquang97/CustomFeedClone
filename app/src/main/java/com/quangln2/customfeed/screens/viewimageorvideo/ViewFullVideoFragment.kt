package com.quangln2.customfeed.screens.viewimageorvideo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.quangln2.customfeed.databinding.FragmentViewFullVideoBinding


class ViewFullVideoFragment : Fragment() {
    private lateinit var binding: FragmentViewFullVideoBinding
    private lateinit var player: Player

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewFullVideoBinding.inflate(inflater, container, false)
        val url = arguments?.getString("url")
        if (url != null && url.contains(".mp4")) {
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
            Glide.with(requireContext()).load(url).into(binding.fullImageView)
        }


        return binding.root
    }

    private fun initializeVideoForLoading(url: String) {
        player = ExoPlayer.Builder(requireContext()).build()
        binding.fullVideoView.player = player
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
    }
}