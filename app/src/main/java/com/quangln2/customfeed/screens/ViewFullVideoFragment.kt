package com.quangln2.customfeed.screens

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.quangln2.customfeed.databinding.FragmentViewFullVideoBinding


class ViewFullVideoFragment : Fragment() {
    private lateinit var binding: FragmentViewFullVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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

            val uri = Uri.parse(url)
            val mediaController = MediaController(context)
            mediaController.setAnchorView(binding.fullVideoView)
            binding.fullVideoView.setMediaController(mediaController)
            binding.fullVideoView.setVideoURI(uri)
            binding.fullVideoView.requestFocus()

            binding.fullVideoProgressBar.visibility = View.INVISIBLE
            binding.fullVideoPlayButton.visibility = View.VISIBLE

            binding.fullVideoView.setOnInfoListener(
                object : MediaPlayer.OnInfoListener {
                    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                        if(mp?.isPlaying == true){
                            binding.fullVideoPlayButton.visibility = View.INVISIBLE
                            return true
                        }
                        return false
                    }
                }
            )

            binding.fullVideoView.setOnCompletionListener {
                binding.fullVideoPlayButton.visibility = View.VISIBLE
            }

        } else {
            binding.fullVideoView.visibility = View.GONE
            binding.fullImageView.visibility = View.VISIBLE
            Glide.with(requireContext()).load(url).into(binding.fullImageView)

        }


        return binding.root
    }
}