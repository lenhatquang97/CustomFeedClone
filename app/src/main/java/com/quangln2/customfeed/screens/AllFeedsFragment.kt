package com.quangln2.customfeed.screens

import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ui.PlayerView
import com.quangln2.customfeed.R
import com.quangln2.customfeed.customview.CustomGridGroup
import com.quangln2.customfeed.databinding.FragmentAllFeedsBinding
import com.quangln2.customfeed.viewmodel.FeedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AllFeedsFragment : Fragment() {
    private lateinit var binding: FragmentAllFeedsBinding
    private lateinit var adapterVal: FeedListAdapter
    private val viewModel: FeedViewModel by activityViewModels()
    private var globalIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAllFeeds()
        println("Once time")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllFeedsBinding.inflate(inflater, container, false)
        val onDeleteItem = fun(id: String) {
            viewModel.deleteFeed(id)
        }
        val onClickAddPost = fun() {
            findNavController().navigate(R.id.action_allFeedsFragment_to_homeScreenFragment)
        }

        adapterVal = FeedListAdapter(requireContext(), onDeleteItem, onClickAddPost)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.allFeeds.apply {
            adapter = adapterVal
            layoutManager = linearLayoutManager
        }
        binding.allFeeds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val itemPosition = linearLayoutManager.findFirstVisibleItemPosition()
                if (itemPosition > 0 && globalIndex != itemPosition) {
                    globalIndex = itemPosition
                    val viewItem = linearLayoutManager.findViewByPosition(itemPosition)
                    val customGridGroup = viewItem?.findViewById<CustomGridGroup>(R.id.customGridGroup)
                    lifecycleScope.launch(Dispatchers.Main){
                        for (i in 0 until customGridGroup?.size!!) {
                            val view = customGridGroup.getChildAt(i)
                            if (view is PlayerView) {
                                if(view.player != null) {
                                    Toast.makeText(requireContext(), "Player completed", Toast.LENGTH_SHORT).show()
                                    view.player?.play()
                                } else {
                                    Toast.makeText(requireContext(), "Player null", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    println("Custom grid group ${customGridGroup?.size}")
                }

            }
        })

        viewModel.uploadLists.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                adapterVal.submitList(it.toMutableList())
            }
        }



        return binding.root
    }


}