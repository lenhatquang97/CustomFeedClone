package com.quangln2.customfeed.ui.screens.allfeeds

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.constants.ConstantClass
import com.quangln2.customfeed.data.constants.ConstantSetup
import com.quangln2.customfeed.data.controllers.FeedController
import com.quangln2.customfeed.data.database.FeedDatabase
import com.quangln2.customfeed.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.data.models.others.EnumFeedLoadingCode
import com.quangln2.customfeed.data.models.others.EnumFeedSplashScreenState
import com.quangln2.customfeed.data.models.uimodel.MyPostRender
import com.quangln2.customfeed.data.models.uimodel.TypeOfPost
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.databinding.FragmentAllFeedsBinding
import com.quangln2.customfeed.others.callback.EventFeedCallback
import com.quangln2.customfeed.others.utils.DownloadUtils
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.ui.viewmodel.FeedViewModel
import com.quangln2.customfeed.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger


class AllFeedsFragment : Fragment() {
    private lateinit var binding: FragmentAllFeedsBinding
    private lateinit var adapterVal: FeedListAdapter

    private val database by lazy { FeedDatabase.getFeedDatabase(requireContext()) }
    private val currentViewRect by lazy { Rect() }
    private val positionDeletedOrRefreshed by lazy { AtomicInteger(-1) }

    val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }

    private lateinit var phoneStateReceiver: BroadcastReceiver

    private val eventCallback: EventFeedCallback
        get() = object : EventFeedCallback {
            override fun onDeleteItem(id: String, position: Int) {
                viewModel.deleteFeed(id, requireContext())
                positionDeletedOrRefreshed.set(position)
            }

            override fun onClickAddPost() =
                findNavController().navigate(R.id.action_allFeedsFragment_to_homeScreenFragment, null, navOptions {
                    anim {
                        enter = android.R.animator.fade_in
                        exit = android.R.animator.fade_out
                    }
                })

            override fun onClickVideoView(currentVideoPosition: Long, url: String, listOfUrls: ArrayList<String>) =
                findNavController().navigate(
                    R.id.action_allFeedsFragment_to_viewFullVideoFragment,
                    Bundle().apply {
                        putLong("currentVideoPosition", currentVideoPosition)
                        putString("value", url)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAllFeedsWithPreloadCache()
        phoneStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Toast.makeText(context, "Phone state changed", Toast.LENGTH_SHORT).show()
                pauseVideo()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllFeedsBinding.inflate(inflater, container, false)
        adapterVal = FeedListAdapter(requireContext(), eventCallback)
        adapterVal.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        val linearLayoutManager = LinearLayoutManager(requireContext())

        binding.allFeeds.apply {
            adapter = adapterVal
            layoutManager = linearLayoutManager
            animation = null
        }

        binding.allFeeds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
                    if (firstVisibleItemPosition > 0) {
                        val item = viewModel.uploadLists.value?.get(firstVisibleItemPosition - 1)
                        if (item != null) {
                            viewModel.downloadResourceWithId(item.resources, requireContext())
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val manager = recyclerView.layoutManager as LinearLayoutManager
                val firstPartiallyIndex = manager.findFirstVisibleItemPosition()
                val tmpFirstFullIndex = manager.findFirstCompletelyVisibleItemPosition()
                val firstFullIndex = if(tmpFirstFullIndex <= 0) 1 else tmpFirstFullIndex
                val lastFullIndex = manager.findLastCompletelyVisibleItemPosition()
                val lastPartiallyIndex = manager.findLastVisibleItemPosition()
                val indexLists = listOf(firstPartiallyIndex, firstFullIndex, lastFullIndex, lastPartiallyIndex)
                val ls = if(dy >= 0) indexLists else indexLists.reversed()
                for(i in ls){
                    if(checkViewHasVideo(i)){
                        var isPlayed = false
                        val onPlayed = fun(){
                            isPlayed = true
                        }
                        scrollToPlayVideoInPosition(i, manager, onPlayed)
                        if(isPlayed) break
                    }
                }
            }
        })

        viewModel.uploadLists.observe(viewLifecycleOwner) {
            it?.apply {
                binding.noPostId.root.visibility = View.VISIBLE
                if (viewModel.feedLoadingCode.value == EnumFeedLoadingCode.SUCCESS.value || it.isNotEmpty()) {
                    binding.noPostId.root.visibility = View.INVISIBLE
                    lifecycleScope.launch(Dispatchers.IO) {
                        val listsOfPostRender = mutableListOf<MyPostRender>()
                        val addNewPostItem = MyPostRender.convertMyPostToMyPostRender(MyPost(), TypeOfPost.ADD_NEW_POST)
                        listsOfPostRender.add(addNewPostItem)
                        it.forEach { itr ->
                            val myPostRender = MyPostRender.convertMyPostToMyPostRender(itr)
                            retrieveFirstImageOrFirstVideo(myPostRender)
                            listsOfPostRender.add(myPostRender)
                        }
                        withContext(Dispatchers.Main) {
                            adapterVal.submitList(listsOfPostRender.toMutableList()){
                                if(positionDeletedOrRefreshed.get() >= 1){
                                    binding.allFeeds.scrollToPosition(positionDeletedOrRefreshed.get() - 1)
                                    positionDeletedOrRefreshed.set(-1)
                                }
                            }
                        }
                    }
                } else {
                    val feedLoadingCode = viewModel.feedLoadingCode.value
                    if (feedLoadingCode != null) {
                        if (feedLoadingCode != EnumFeedLoadingCode.SUCCESS.value && feedLoadingCode != EnumFeedLoadingCode.INITIAL.value) {
                            binding.noPostId.root.visibility = View.VISIBLE
                        } else if (feedLoadingCode == EnumFeedLoadingCode.SUCCESS.value) {
                            binding.noPostId.root.visibility = View.GONE
                            val listsOfPostRender = mutableListOf<MyPostRender>()
                            val addNewPostItem = MyPostRender.convertMyPostToMyPostRender(MyPost(), TypeOfPost.ADD_NEW_POST)
                            listsOfPostRender.add(addNewPostItem)
                            adapterVal.submitList(listsOfPostRender.toMutableList()){
                                if(positionDeletedOrRefreshed.get() >= 1){
                                    binding.allFeeds.scrollToPosition(positionDeletedOrRefreshed.get() - 1)
                                    positionDeletedOrRefreshed.set(-1)
                                }
                            }
                        }
                    }
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }

        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.noPostId.imageView.visibility = View.VISIBLE
            binding.noPostId.textNote.text = resources.getString(R.string.loading)
            positionDeletedOrRefreshed.set(1)
            viewModel.getAllFeeds()
        }

        FeedController.isLoading.observe(viewLifecycleOwner) {
            binding.loadingCard.root.visibility = if (it == EnumFeedSplashScreenState.LOADING.value) View.VISIBLE else View.GONE
            if (it == EnumFeedSplashScreenState.COMPLETE.value) {
                binding.swipeRefreshLayout.post {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                binding.noPostId.imageView.visibility = View.VISIBLE
                binding.noPostId.textNote.text = resources.getString(R.string.loading)
                viewModel.getAllFeeds()
                FeedController.isLoading.value = EnumFeedSplashScreenState.UNDEFINED.value

            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.PHONE_STATE")
        requireActivity().registerReceiver(phoneStateReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        //Save for next use when we switch to another window.
        val (mainItemIndex, videoIndex) = FeedController.peekVideoQueue()
        if (mainItemIndex != null && videoIndex != null) {
            FeedController.addedToQueue(mainItemIndex, videoIndex)
            FeedController.addedToPlayedVideos(mainItemIndex, videoIndex)
        }
        pauseAndReleaseVideo()

    }

    override fun onStart() {
        super.onStart()
        calculateVisibilityVideoView()
    }

    override fun onDestroy() {
        super.onDestroy()
        try{
            requireContext().unregisterReceiver(phoneStateReceiver)
        } catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun checkViewHasVideo(itemPosition: Int): Boolean{
        if(itemPosition <= 0) return false
        if(itemPosition < adapterVal.itemCount){
            val item = adapterVal.currentList[itemPosition]
            return item.containsVideo
        }
        return false
    }

    private fun scrollToPlayVideoInPosition(itemPosition: Int, linearLayoutManager: LinearLayoutManager, onPlayed: () -> Unit = {}) {
        val viewItem = linearLayoutManager.findViewByPosition(itemPosition)
        val firstCondition = (itemPosition > 0 && viewItem != null)
        val secondCondition = (viewItem != null && itemPosition == adapterVal.itemCount - 1)

        if (firstCondition || secondCondition) {
            val customGridGroup = viewItem?.findViewById<FrameLayout>(R.id.customGridGroup)
            if (customGridGroup != null) {
                for (i in 0 until customGridGroup.size) {
                    val view = customGridGroup.getChildAt(i)
                    if (FeedController.isViewAddedToQueue(view, itemPosition, i)) break
                }
                if (FeedController.videoQueueSize() == 1) {
                    calculateVisibilityVideoView(onPlayed)
                } else if (FeedController.videoQueueSize() > 1) {
                    if (!viewModel.checkWhetherHaveMoreThanTwoVideosInPost()) {
                        pauseVideo()
                        calculateVisibilityVideoView(onPlayed)
                    }
                }
            }
        }
    }

    private fun pauseVideo() {
        val (pausedItemIndex, videoIndex) = FeedController.peekVideoQueue()
        if (pausedItemIndex != null && videoIndex != null) {
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(pausedItemIndex)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            val view = customGridGroup?.getChildAt(videoIndex)
            if (view is LoadingVideoView) {
                view.pauseVideo()
                FeedController.safeRemoveFromQueue()
            }
        }
    }

    private fun pauseAndReleaseVideo() {
        val (releasedItemIndex, videoIndex) = FeedController.peekVideoQueue()
        if (releasedItemIndex != null && videoIndex != null) {
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(releasedItemIndex)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            val view = customGridGroup?.getChildAt(videoIndex)
            if (view is LoadingVideoView) {
                view.pauseVideo()
                view.releaseVideo()
                FeedController.safeRemoveFromQueue()
            }
        }
    }


    private fun playVideo() {
        val (mainItemIndex, videoIndex) = FeedController.peekVideoQueue()
        if (mainItemIndex != null && videoIndex != null) {
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(mainItemIndex)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            val view = customGridGroup?.getChildAt(videoIndex)

            if (view is LoadingVideoView) {
                view.playVideo()
                view.player.addListener(
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            super.onPlaybackStateChanged(playbackState)
                            var flagEndOfVideoInGrid = false
                            if (playbackState == Player.STATE_ENDED) {
                                //End this video
                                view.player.seekTo(0)
                                view.pauseVideo()
                                FeedController.safeRemoveFromQueue()

                                //Play next video in list
                                for (i in videoIndex until customGridGroup.size) {
                                    val nextView = customGridGroup.getChildAt(i)
                                    if (nextView is LoadingVideoView && i != videoIndex) {
                                        //Less than 9 images or videos
                                        val condition1 = customGridGroup.size <= 9 && i < ConstantClass.MAXIMUM_IMAGE_IN_A_GRID
                                        //More than 10 images or videos with CustomLayer in 8th position
                                        val condition2 = customGridGroup.size > 9 && i < ConstantClass.MAXIMUM_IMAGE_IN_A_GRID - 1
                                        if (condition1 || condition2) {
                                            FeedController.addedToQueue(mainItemIndex, i)
                                            FeedController.addedToPlayedVideos(mainItemIndex, i)
                                            calculateVisibilityVideoView()
                                            flagEndOfVideoInGrid = true
                                            break
                                        }
                                    }
                                }

                                //Play from start if end of video
                                if (!flagEndOfVideoInGrid) {
                                    val firstVideoIndex = customGridGroup.children.indexOfFirst { it is LoadingVideoView }
                                    if (firstVideoIndex != -1) {
                                        FeedController.addedToQueue(mainItemIndex, firstVideoIndex)
                                        FeedController.addedToPlayedVideos(mainItemIndex, firstVideoIndex)
                                        calculateVisibilityVideoView()
                                    }
                                }
                            }
                        }
                    }
                )
            } else FeedController.safeRemoveFromQueue()
        }
    }

    private fun retrieveFirstImageOrFirstVideo(myPostRender: MyPostRender) {
        if (myPostRender.resources.size > 0) {
            val url = myPostRender.resources[0].url
            val size = myPostRender.resources[0].size
            val doesLocalFileExist = DownloadUtils.doesLocalFileExist(url, requireContext())
            val isValidFile = DownloadUtils.isValidFile(url, requireContext(), size)
            val temporaryFilePath = DownloadUtils.getTemporaryFilePath(url, requireContext())
            val value = if (doesLocalFileExist && isValidFile) temporaryFilePath else url
            Glide.with(requireContext()).load(value).into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    myPostRender.firstItemWidth = resource.intrinsicWidth
                    myPostRender.firstItemHeight = resource.intrinsicHeight
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        pauseVideo()
    }

    private fun calculateVisibilityVideoView(onPlayed: () -> Unit = {}) {
        var (mainItemIndex, videoIndex) = FeedController.peekVideoQueue()
        if (mainItemIndex != null && videoIndex != null) {
            val tmp = FeedController.getPlayedVideos(mainItemIndex)
            if(tmp != -1 && videoIndex != tmp){
                videoIndex = tmp
                FeedController.safeRemoveFromQueue()
                FeedController.addedToQueue(mainItemIndex, videoIndex)
            }


            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(mainItemIndex)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            val view = customGridGroup?.getChildAt(videoIndex)
            if (view is LoadingVideoView) {
                view.getLocalVisibleRect(currentViewRect)
                val height = currentViewRect.height()
                val isOutOfBoundsOnTheTop = currentViewRect.bottom < 0 && currentViewRect.top < 0
                val isOutOfBoundsAtTheBottom =
                    currentViewRect.top >= ConstantSetup.PHONE_HEIGHT && currentViewRect.bottom >= ConstantSetup.PHONE_HEIGHT
                if (isOutOfBoundsAtTheBottom || isOutOfBoundsOnTheTop) {
                    pauseVideo()
                } else {
                    val percents = height * 100 / view.height
                    if (percents >= 50) {
                        playVideo()
                        onPlayed()
                    } else {
                        pauseVideo()
                    }
                }
            }
        }
    }


}