package com.quangln2.customfeedui.ui.screens.viewimageorvideo

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.exoplayer2.ExoPlayer

class FullImageVideoAdapter(
    fragment: Fragment,
    private val listOfUrls: ArrayList<String>,
    private val currentVideoPosition: Long,
    private val player: ExoPlayer,
    private val id: String
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return listOfUrls.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = ImageOrVideoFragment(player)
        fragment.arguments = Bundle().apply {
            putLong("currentVideoPosition", currentVideoPosition)
            putInt("position", position)
            putStringArrayList("listOfUrls", listOfUrls)
            putString("id", id)
        }
        return fragment
    }

}