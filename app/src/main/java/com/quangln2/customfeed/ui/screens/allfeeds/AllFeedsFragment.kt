package com.quangln2.customfeed.ui.screens.allfeeds

import android.os.Bundle
import android.util.Log
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
import com.quangln2.customfeed.data.controllers.FeedController
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.controllers.VideoPlayed
import com.quangln2.customfeed.others.callback.EventFeedCallback
import com.quangln2.customfeed.ui.customview.CustomGridGroup
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.databinding.FragmentAllFeedsBinding
import com.quangln2.customfeed.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.ui.viewmodel.FeedViewModel
import com.quangln2.customfeed.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AllFeedsFragment : Fragment() {
    private lateinit var binding: FragmentAllFeedsBinding
    private lateinit var adapterVal: FeedListAdapter
    private val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(), RemoteDataSourceImpl()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAllFeeds()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllFeedsBinding.inflate(inflater, container, false)
        val eventCallback = object : EventFeedCallback {
            override fun onDeleteItem(id: String) = viewModel.deleteFeed(id)
            override fun onClickAddPost() =
                findNavController().navigate(R.id.action_allFeedsFragment_to_homeScreenFragment)

            override fun onClickVideoView(url: String) = findNavController().navigate(
                R.id.action_allFeedsFragment_to_viewFullVideoFragment,
                Bundle().apply { putString("url", url) })

            override fun onClickViewMore(id: String) = findNavController().navigate(
                R.id.action_allFeedsFragment_to_viewMoreFragment,
                Bundle().apply { putString("id", id) })
        }

        adapterVal = FeedListAdapter(requireContext(), eventCallback)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.allFeeds.apply {
            adapter = adapterVal
            layoutManager = linearLayoutManager
        }
        binding.allFeeds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val itemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                scrollToPlayVideoInPosition(itemPosition, linearLayoutManager)
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
        val (pausedItemIndex, videoIndex) = FeedController.peekVideoQueue()
        CoroutineScope(Dispatchers.Main).launch {
            if (pausedItemIndex != null && videoIndex != null) {
                val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(pausedItemIndex)
                val customGridGroup = viewItem?.itemView?.findViewById<CustomGridGroup>(R.id.customGridGroup)
                val view = customGridGroup?.getChildAt(videoIndex)
                if (view is LoadingVideoView) {
                    view.pauseVideo()
                    FeedController.safeRemoveFromQueue()
                }
            }

        }

    }

    private fun scrollToPlayVideoInPosition(itemPosition: Int, linearLayoutManager: LinearLayoutManager) {
        val viewItem = linearLayoutManager.findViewByPosition(itemPosition)
        val firstCondition = (itemPosition > 0 && viewItem != null)
        val secondCondition = (viewItem != null && itemPosition == adapterVal.itemCount - 1)

        if (firstCondition || secondCondition) {
            val customGridGroup = viewItem?.findViewById<CustomGridGroup>(R.id.customGridGroup)
            lifecycleScope.launch(Dispatchers.Main) {
                for (i in 0 until customGridGroup?.size!!) {
                    val view = customGridGroup.getChildAt(i)
                    if (FeedController.isViewAddedToQueue(view, itemPosition, i)) break
                }
                if (FeedController.videoQueue.size == 1) {
                    playVideo()
                } else if (FeedController.videoQueue.size > 1) {
                    Log.d("CustomFeed", "Queue size ${FeedController.videoQueue.size}")
                    Log.d("CustomFeed", FeedController.videoQueue.joinToString { it.toString() })
                    if(!checkWhetherHaveMoreThanTwoVideosInPost()) {
                        pauseVideo()
                        playVideo()
                    }

                }
            }
        } else {
            pauseVideo()
        }
    }

    private fun checkWhetherHaveMoreThanTwoVideosInPost(): Boolean {
        val (mainItemIndex, videoIndex) = FeedController.popVideoQueue()
        val (anotherMainItemIndex, anotherVideoIndex) = FeedController.popVideoQueue()
        if (mainItemIndex != null && videoIndex != null && anotherMainItemIndex != null && anotherVideoIndex != null) {
            FeedController.videoQueue.add(VideoPlayed(mainItemIndex, videoIndex))
            FeedController.videoQueue.add(VideoPlayed(anotherMainItemIndex, anotherVideoIndex))
            if(mainItemIndex == anotherMainItemIndex) return true
        }
        return false
    }


    private fun playVideo() {
        val (mainItemIndex, videoIndex) = FeedController.peekVideoQueue()
        if (mainItemIndex != null && videoIndex != null) {
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(mainItemIndex)
            val customGridGroup = viewItem?.itemView?.findViewById<CustomGridGroup>(R.id.customGridGroup)
            val view = customGridGroup?.getChildAt(videoIndex)

            if (view is LoadingVideoView) {
                view.playVideo()
                view.player.addListener(
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            super.onPlaybackStateChanged(playbackState)
                            if (playbackState == Player.STATE_ENDED) {
                                view.player.seekTo(0)
                                view.pauseVideo()

                                FeedController.safeRemoveFromQueue()

                                //Play next video in list
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
            } else FeedController.safeRemoveFromQueue()
        }

    }

    override fun onStop() {
        super.onStop()
        pauseVideo()

        //Save for next use when we switch to another window.
        val (mainItemIndex, videoIndex) = FeedController.peekVideoQueue()
        if (mainItemIndex != null && videoIndex != null) {
            FeedController.videoQueue.add(VideoPlayed(mainItemIndex, videoIndex))
        }

    }

    override fun onStart() {
        super.onStart()
        //Replay this video again
        playVideo()
    }
}