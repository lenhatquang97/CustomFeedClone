package com.quangln2.customfeed.screens.allfeeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.Player
import com.quangln2.customfeed.FeedController
import com.quangln2.customfeed.R
import com.quangln2.customfeed.VideoPlayed
import com.quangln2.customfeed.customview.CustomGridGroup
import com.quangln2.customfeed.customview.LoadingVideoView
import com.quangln2.customfeed.databinding.FragmentAllFeedsBinding
import com.quangln2.customfeed.screens.FeedListAdapter
import com.quangln2.customfeed.viewmodel.FeedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AllFeedsFragment : Fragment() {
    private lateinit var binding: FragmentAllFeedsBinding
    private lateinit var adapterVal: FeedListAdapter
    private val viewModel: FeedViewModel by activityViewModels()
    private var globalIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAllFeeds()
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
        val onClickVideoView = fun(url: String) {
            val bundle = Bundle()
            bundle.putString("url", url)
            findNavController().navigate(R.id.action_allFeedsFragment_to_viewFullVideoFragment, bundle)
        }

        val onClickViewMore = fun() {
            findNavController().navigate(R.id.action_allFeedsFragment_to_viewMoreFragment)
        }

        adapterVal = FeedListAdapter(requireContext(), onDeleteItem, onClickAddPost, onClickVideoView, onClickViewMore)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.allFeeds.apply {
            adapter = adapterVal
            layoutManager = linearLayoutManager
        }
        binding.allFeeds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val itemPosition = linearLayoutManager.findLastVisibleItemPosition()
                scrollToPlayVideoInPosition(itemPosition, linearLayoutManager, recyclerView)
            }
        })

        viewModel.uploadLists.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                adapterVal.submitList(it.toMutableList())
            }
        }

        return binding.root
    }

    private fun pauseVideo() {
        val pausedItemIndex = FeedController.videoQueue.peek()?.itemPosition
        val videoIndex = FeedController.videoQueue.peek()?.index
        val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(pausedItemIndex!!)
        val customGridGroup = viewItem?.itemView?.findViewById<CustomGridGroup>(R.id.customGridGroup)
        val view = customGridGroup?.getChildAt(videoIndex!!)
        if (view is LoadingVideoView) {
            view.pauseVideo()
            FeedController.videoQueue.remove()
        }
    }

    private fun scrollToPlayVideoInPosition(itemPosition: Int, linearLayoutManager: LinearLayoutManager,
                                            recyclerView: RecyclerView) {
        val viewItem = linearLayoutManager.findViewByPosition(itemPosition)
        val firstCondition =
            (itemPosition > 0 && viewItem != null && viewItem.top <= recyclerView.height)
        val secondCondition = (viewItem != null && itemPosition == adapterVal.itemCount - 1)

        if (firstCondition || secondCondition) {
            globalIndex = itemPosition
            val customGridGroup = viewItem?.findViewById<CustomGridGroup>(R.id.customGridGroup)
            lifecycleScope.launch(Dispatchers.Main) {
                for (i in 0 until customGridGroup?.size!!) {
                    val view = customGridGroup.getChildAt(i)
                    if (view is LoadingVideoView) {
                        FeedController.videoQueue.add(VideoPlayed(itemPosition, i))
                        break
                    }
                }
                if (FeedController.videoQueue.size == 1) {
                    playVideo()
                } else if (FeedController.videoQueue.size > 1) {
                    pauseVideo()
                    playVideo()
                }
            }
        }
    }

    private fun playVideo() {
        val mainItemIndex = FeedController.videoQueue.peek()?.itemPosition
        val videoIndex = FeedController.videoQueue.peek()?.index
        val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(mainItemIndex!!)
        val customGridGroup = viewItem?.itemView?.findViewById<CustomGridGroup>(R.id.customGridGroup)
        val view = customGridGroup?.getChildAt(videoIndex!!)

        if (view is LoadingVideoView && videoIndex != null) {
            view.playVideo()
            view.player.addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        if (playbackState == Player.STATE_ENDED) {
                            view.pauseVideo()

                            if (FeedController.videoQueue.size >= 1) {
                                FeedController.videoQueue.remove()
                            }

                            for (i in videoIndex until customGridGroup.size) {
                                val nextView = customGridGroup.getChildAt(i)
                                if (nextView is LoadingVideoView && i != videoIndex) {
                                    FeedController.videoQueue.add(VideoPlayed(mainItemIndex, i))
                                    playVideo()
                                    break
                                }
                            }
                        }
                    }
                }
            )
        } else {
            if (FeedController.videoQueue.size >= 1) {
                FeedController.videoQueue.remove()
            }
        }
    }


}