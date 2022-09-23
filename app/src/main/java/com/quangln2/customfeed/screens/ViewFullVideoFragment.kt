package com.quangln2.customfeed.screens

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
    ): View? {
        binding = FragmentViewFullVideoBinding.inflate(inflater, container, false)
        val url = arguments?.getString("url")
        if (url != null && url.contains(".mp4")) {
            binding.fullVideoView.visibility = View.VISIBLE
            binding.fullImageView.visibility = View.GONE

            val uri = Uri.parse(url)
            val mediaController = MediaController(context)
            binding.fullVideoView.setMediaController(mediaController)
            binding.fullVideoView.setVideoURI(uri)
            binding.fullVideoView.requestFocus()


        } else {
            binding.fullVideoView.visibility = View.GONE
            binding.fullImageView.visibility = View.VISIBLE

            Glide.with(requireContext()).load(url).into(binding.fullImageView)

        }



        return binding.root
    }
}