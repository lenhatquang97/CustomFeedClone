package com.quangln2.customfeedui.ui.screens.viewimageorvideo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.quangln2.customfeedui.databinding.FragmentViewFullVideoBinding


class ViewFullVideoFragment : Fragment() {
    private lateinit var binding: FragmentViewFullVideoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewFullVideoBinding.inflate(inflater, container, false)
        val value = arguments?.getString("value")
        val listOfUrls = arguments?.getStringArrayList("listOfUrls")
        val currentVideoPosition = arguments?.getLong("currentVideoPosition") ?: -1
        val player = ExoPlayer.Builder(requireContext()).build()
        if (value != null && listOfUrls != null) {
            binding.viewPager.adapter = FullImageVideoAdapter(this, listOfUrls, currentVideoPosition, player)
            binding.viewPager.setCurrentItem(listOfUrls.indexOf(value), false)
        }
        return binding.root
    }


}