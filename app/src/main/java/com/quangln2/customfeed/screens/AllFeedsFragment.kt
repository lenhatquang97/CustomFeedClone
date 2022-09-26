package com.quangln2.customfeed.screens

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
import com.quangln2.customfeed.FeedController
import com.quangln2.customfeed.R
import com.quangln2.customfeed.VideoPlayed
import com.quangln2.customfeed.customview.CustomGridGroup
import com.quangln2.customfeed.customview.LoadingVideoView
import com.quangln2.customfeed.databinding.FragmentAllFeedsBinding
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
        val onClickVideoView = fun(url: String) {
            val bundle = Bundle()
            bundle.putString("url", url)
            findNavController().navigate(R.id.action_allFeedsFragment_to_viewFullVideoFragment, bundle)
        }

        adapterVal = FeedListAdapter(requireContext(), onDeleteItem, onClickAddPost, onClickVideoView)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.allFeeds.apply {
            adapter = adapterVal
            layoutManager = linearLayoutManager
        }
        binding.allFeeds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val itemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                if (itemPosition > 0 && globalIndex != itemPosition) {
                    globalIndex = itemPosition
                    val viewItem = linearLayoutManager.findViewByPosition(itemPosition)
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
                            playVideo(linearLayoutManager)
                        } else if (FeedController.videoQueue.size > 1) {
                            pauseVideo(linearLayoutManager)
                            playVideo(linearLayoutManager)
                        }

                    }
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

    private fun pauseVideo(linearLayoutManager: LinearLayoutManager) {
        val pausedItemIndex = FeedController.videoQueue.peek()?.itemPosition
        val videoIndex = FeedController.videoQueue.peek()?.index
        val viewItem = linearLayoutManager.findViewByPosition(pausedItemIndex!!)
        val customGridGroup = viewItem?.findViewById<CustomGridGroup>(R.id.customGridGroup)
        val view = customGridGroup?.getChildAt(videoIndex!!)
        if (view is LoadingVideoView) {
            if(view.mediaPlayer != null && view.mediaPlayer?.isPlaying == true){
                view.mediaPlayer?.pause()
            }
            FeedController.videoQueue.remove()
        }
    }

    private fun playVideo(linearLayoutManager: LinearLayoutManager) {
        val mainItemIndex = FeedController.videoQueue.peek()?.itemPosition
        val videoIndex = FeedController.videoQueue.peek()?.index
        val viewItem = linearLayoutManager.findViewByPosition(mainItemIndex!!)
        val customGridGroup = viewItem?.findViewById<CustomGridGroup>(R.id.customGridGroup)
        val view = customGridGroup?.getChildAt(videoIndex!!)
        if (view is LoadingVideoView && videoIndex != null) {
            view.playButton.visibility = View.INVISIBLE
            view.progressBar.visibility = View.GONE

            if(view.mediaPlayer != null){
                println("Play video in AllFeedsFragment")

                view.mediaPlayer?.start()
            }
            view.mediaPlayer?.setOnCompletionListener {
                view.playButton.visibility = View.VISIBLE
                if (FeedController.videoQueue.size >= 1) {
                    FeedController.videoQueue.remove()
                }
                for (i in videoIndex until customGridGroup.size) {
                    val nextView = customGridGroup.getChildAt(i)
                    if (nextView is LoadingVideoView && i != videoIndex) {
                        FeedController.videoQueue.add(VideoPlayed(mainItemIndex, i))
                        playVideo(linearLayoutManager)
                        break
                    }
                }

            }
        }
    }


}