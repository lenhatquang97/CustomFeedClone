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
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.Player
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.controllers.FeedController
import com.quangln2.customfeed.data.controllers.VideoPlayed
import com.quangln2.customfeed.data.database.FeedDatabase
import com.quangln2.customfeed.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.data.models.uimodel.MyPostRender
import com.quangln2.customfeed.data.models.uimodel.TypeOfPost
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.databinding.FragmentAllFeedsBinding
import com.quangln2.customfeed.others.callback.EventFeedCallback
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.ui.customview.customgrid.CustomGridGroup
import com.quangln2.customfeed.ui.viewmodel.FeedViewModel
import com.quangln2.customfeed.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AllFeedsFragment : Fragment() {
    private lateinit var binding: FragmentAllFeedsBinding
    private lateinit var adapterVal: FeedListAdapter

    private val database by lazy {
        FeedDatabase.getFeedDatabase(requireContext())
    }
    private val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAllFeedsWithPreloadCache(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllFeedsBinding.inflate(inflater, container, false)
        val eventCallback = object : EventFeedCallback {
            override fun onDeleteItem(id: String) {
                viewModel.deleteFeed(id)
            }

            override fun onClickAddPost() =
                findNavController().navigate(R.id.action_allFeedsFragment_to_homeScreenFragment, null, navOptions {
                    anim {
                        enter = android.R.animator.fade_in
                        exit = android.R.animator.fade_out
                    }
                })

            override fun onClickVideoView(value: String, listOfUrls: ArrayList<String>) = findNavController().navigate(
                R.id.action_allFeedsFragment_to_viewFullVideoFragment,
                Bundle().apply {
                    putString("value", value)
                    putStringArrayList("listOfUrls", listOfUrls)
                },
                navOptions {
                    anim {
                        enter = android.R.animator.fade_in
                        exit = android.R.animator.fade_out
                    }
                }
            )

            override fun onClickViewMore(id: String) = findNavController().navigate(
                R.id.action_allFeedsFragment_to_viewMoreFragment,
                Bundle().apply { putString("id", id) },
                navOptions {
                    anim {
                        enter = android.R.animator.fade_in
                        exit = android.R.animator.fade_out
                    }
                }
            )
        }

        adapterVal = FeedListAdapter(requireContext(), eventCallback)
        val linearLayoutManager = LinearLayoutManager(requireContext())

        binding.allFeeds.apply {
            adapter = adapterVal
            layoutManager = linearLayoutManager
            animation = null
        }
        binding.allFeeds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val manager = recyclerView.layoutManager
                if (manager is LinearLayoutManager) {
                    val itemPosition = manager.findFirstCompletelyVisibleItemPosition()
                    scrollToPlayVideoInPosition(itemPosition, manager)
                }

            }
        })

        viewModel.uploadLists.observe(viewLifecycleOwner) {
            binding.noPostId.root.visibility = View.VISIBLE
            val condition1 = it != null && it.size >= 1 && viewModel.feedLoadingCode.value == 200
            val condition2 = it != null && it.size >= 2
            if (condition1 || condition2) {
                binding.noPostId.root.visibility = View.INVISIBLE
                val listsOfPostRender = mutableListOf<MyPostRender>()
                listsOfPostRender.add(MyPostRender.convertMyPostToMyPostRender(MyPost().copy(feedId = "none"), TypeOfPost.ADD_NEW_POST))
                it.forEach { itr -> listsOfPostRender.add(MyPostRender.convertMyPostToMyPostRender(itr)) }
                println("Render size: ${listsOfPostRender.joinToString { it.caption }}")
                adapterVal.submitList(listsOfPostRender.toMutableList())
            } else {
                if (viewModel.feedLoadingCode.value != null) {
                    if (viewModel.feedLoadingCode.value!! != 200 && viewModel.feedLoadingCode.value!! != 0) {
                        binding.noPostId.root.visibility = View.VISIBLE
                        binding.noPostId.alertView.visibility = View.VISIBLE
                        binding.noPostId.imageView.visibility = View.INVISIBLE
                        binding.noPostId.textNote.text =
                            "Sorry that we can't load your feed cache. Swipe down to try again.\n Exception code: ${viewModel.feedLoadingCode.value}"
                    }
                }
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.noPostId.alertView.visibility = View.INVISIBLE
            binding.noPostId.imageView.visibility = View.VISIBLE
            binding.noPostId.textNote.text = "Loading"
            viewModel.getAllFeeds(requireContext())
            binding.swipeRefreshLayout.isRefreshing = false

        }


        FeedController.isLoading.observe(viewLifecycleOwner){
            //1 means loading, 0 means complete loading, but -1 means undefined
            binding.loadingCard.root.visibility = if(it == 1) View.VISIBLE else View.GONE
            if(it == 0){
                viewModel.getAllFeeds(requireContext())
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
            if (customGridGroup != null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    for (i in 0 until customGridGroup.size) {
                        val view = customGridGroup.getChildAt(i)
                        if (FeedController.isViewAddedToQueue(view, itemPosition, i)) break
                    }
                    if (FeedController.videoQueue.size == 1) {
                        playVideo()
                    } else if (FeedController.videoQueue.size > 1) {
                        Log.d("CustomFeed", "Queue size ${FeedController.videoQueue.size}")
                        Log.d("CustomFeed", FeedController.videoQueue.joinToString { it.toString() })
                        if (!checkWhetherHaveMoreThanTwoVideosInPost()) {
                            pauseVideo()
                            playVideo()
                        }

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
            if (mainItemIndex == anotherMainItemIndex) return true
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
                                    if (nextView is LoadingVideoView && i != videoIndex && i < 9) {
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