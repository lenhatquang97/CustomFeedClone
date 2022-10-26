package com.quangln2.customfeed.ui.screens.viewimageorvideo

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FullImageVideoAdapter(
    fragment: Fragment,
    private val listOfUrls: ArrayList<String>,
    private val currentVideoPosition: Long
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return listOfUrls.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = ImageOrVideoFragment()
        fragment.arguments = Bundle().apply {
            putLong("currentVideoPosition", currentVideoPosition)
            putInt("position", position)
            putStringArrayList("listOfUrls", listOfUrls)
        }
        return fragment
    }

}