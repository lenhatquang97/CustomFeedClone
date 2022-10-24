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
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.net.toUri
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
import com.quangln2.customfeed.others.utils.DownloadUtils
import com.quangln2.customfeed.others.utils.FileUtils
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.ui.viewmodel.FeedViewModel
import com.quangln2.customfeed.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AllFeedsFragment : Fragment() {
    lateinit var binding: FragmentAllFeedsBinding
    lateinit var adapterVal: FeedListAdapter

    private val database by lazy {
        FeedDatabase.getFeedDatabase(requireContext())
    }
    val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }

    private var feedScrollY = 0
    private val phoneStateReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Toast.makeText(context, "Phone state changed", Toast.LENGTH_SHORT).show()
            pauseVideo()
        }
    }
    private val currentViewRect = Rect()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("SCROLL_Y", feedScrollY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAllFeedsWithPreloadCache(requireContext())
        if(savedInstanceState != null){
           feedScrollY = savedInstanceState.getInt("SCROLL_Y", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllFeedsBinding.inflate(inflater, container, false)

        adapterVal = FeedListAdapter(requireContext(), eventCallback)
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
                val manager = recyclerView.layoutManager
                if (manager is LinearLayoutManager && dy > 0) {
                    val index = manager.findLastVisibleItemPosition()
                    val actualIndex = if(index > 0) index else 1
                    scrollToPlayVideoInPosition(actualIndex, manager, dy)
                } else if(manager is LinearLayoutManager && dy < 0) {
                    val index = manager.findFirstVisibleItemPosition()
                    val actualIndex = if(index > 0) index else 1
                    scrollToPlayVideoInPosition(actualIndex, manager, dy)
                }
                feedScrollY += dy
            }
        })

        viewModel.uploadLists.observe(viewLifecycleOwner) {
            binding.noPostId.root.visibility = View.VISIBLE
            val condition1 = it != null && it.size >= 1 && viewModel.feedLoadingCode.value == 200
            val condition2 = it != null && it.size >= 2
            if (condition1 || condition2) {
                binding.noPostId.root.visibility = View.INVISIBLE
                lifecycleScope.launch(Dispatchers.IO){
                    val listsOfPostRender = mutableListOf<MyPostRender>()
                    val addNewPostItem = MyPostRender.convertMyPostToMyPostRender(MyPost(), TypeOfPost.ADD_NEW_POST)

                    listsOfPostRender.add(addNewPostItem)
                    it.forEach { itr ->
                        val myPostRender = MyPostRender.convertMyPostToMyPostRender(itr)
                        retrieveFirstImageOrFirstVideo(myPostRender)
                        listsOfPostRender.add(myPostRender)
                    }

                    withContext(Dispatchers.Main){
                        adapterVal.submitList(listsOfPostRender.toMutableList()){
                            binding.allFeeds.scrollBy(0, feedScrollY)
                        }

                    }

                }
            } else {
                if (viewModel.feedLoadingCode.value != null) {
                    if (viewModel.feedLoadingCode.value!! != 200 && viewModel.feedLoadingCode.value!! != 0) {
                        binding.noPostId.root.visibility = View.VISIBLE
                    } else if(viewModel.feedLoadingCode.value!! == 200) {
                        binding.noPostId.root.visibility = View.GONE
                        val listsOfPostRender = mutableListOf<MyPostRender>()
                        val addNewPostItem = MyPostRender.convertMyPostToMyPostRender(MyPost(), TypeOfPost.ADD_NEW_POST)
                        listsOfPostRender.add(addNewPostItem)

                        adapterVal.submitList(listsOfPostRender.toMutableList()){
                            binding.allFeeds.scrollBy(0, feedScrollY)
                        }
                    }
                }
            }

            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            pauseVideo()
            binding.noPostId.imageView.visibility = View.VISIBLE
            binding.noPostId.textNote.text = "Loading"
            viewModel.getAllFeeds(requireContext())
        }


        FeedController.isLoading.observe(viewLifecycleOwner) {
            //1 means loading, 0 means complete loading, but -1 means undefined
            binding.loadingCard.root.visibility = if (it == 1) View.VISIBLE else View.GONE
            if (it == 0) {
                binding.swipeRefreshLayout.post {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                binding.noPostId.imageView.visibility = View.VISIBLE
                binding.noPostId.textNote.text = "Loading"
                viewModel.getAllFeeds(requireContext())
                FeedController.isLoading.value = -1

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
        pauseVideo()
        //Save for next use when we switch to another window.
        val (mainItemIndex, videoIndex) = FeedController.peekVideoQueue()
        if (mainItemIndex != null && videoIndex != null) {
            FeedController.videoQueue.add(VideoPlayed(mainItemIndex, videoIndex))
        }

    }

    override fun onStart() {
        super.onStart()
        playVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(phoneStateReceiver)
    }

    private fun scrollToPlayVideoInPosition(itemPosition: Int, linearLayoutManager: LinearLayoutManager, dy: Int) {
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
                if (FeedController.videoQueue.size == 1) {
                    calculateVisibilityVideoView(dy)
                } else if (FeedController.videoQueue.size > 1) {
                    if (!checkWhetherHaveMoreThanTwoVideosInPost()) {
                        pauseVideo()
                        calculateVisibilityVideoView(dy)
                    }
                }
            }
        }
    }


    private fun checkWhetherHaveMoreThanTwoVideosInPost(): Boolean {
        val (mainItemIndex, videoIndex) = FeedController.popVideoQueue()
        val (anotherMainItemIndex, anotherVideoIndex) = FeedController.popVideoQueue()
        if (mainItemIndex != null && videoIndex != null && anotherMainItemIndex != null && anotherVideoIndex != null) {
            FeedController.videoQueue.add(VideoPlayed(mainItemIndex, videoIndex))
            if (mainItemIndex == anotherMainItemIndex) return true
        }
        return false
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


    private fun playVideo(){
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
                                        val condition1 = customGridGroup.size <= 9 && i < ConstantClass.MAXIMUM_IMAGE_IN_A_GRID
                                        val condition2 = customGridGroup.size > 9 && i < ConstantClass.MAXIMUM_IMAGE_IN_A_GRID - 1
                                        if(condition1 || condition2){
                                            FeedController.videoQueue.add(VideoPlayed(mainItemIndex, i))
                                            playVideo()
                                            flagEndOfVideoInGrid = true
                                            break
                                        }
                                    }
                                }

                                //Play from start if end of video
                                if(!flagEndOfVideoInGrid){
                                    val firstVideoIndex = customGridGroup.children.indexOfFirst { it is LoadingVideoView }
                                    if(firstVideoIndex != -1){
                                        FeedController.videoQueue.add(VideoPlayed(mainItemIndex, firstVideoIndex))
                                        playVideo()
                                    }
                                }
                            }
                        }
                    }
                )
            } else FeedController.safeRemoveFromQueue()
        }
    }

    private val eventCallback: EventFeedCallback
        get() = object : EventFeedCallback {
        override fun onDeleteItem(id: String) {
            viewModel.deleteFeed(id, requireContext())
        }

        override fun onClickAddPost() =
            findNavController().navigate(R.id.action_allFeedsFragment_to_homeScreenFragment, null, navOptions {
                anim {
                    enter = android.R.animator.fade_in
                    exit = android.R.animator.fade_out
                }
            })

        override fun onClickVideoView(currentVideoPosition: Long, value: String, listOfUrls: ArrayList<String>) =
            findNavController().navigate(
                R.id.action_allFeedsFragment_to_viewFullVideoFragment,
                Bundle().apply {
                    putLong("currentVideoPosition", currentVideoPosition)
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

    private fun retrieveFirstImageOrFirstVideo(myPostRender: MyPostRender){
        if(myPostRender.resources.size > 0){
            val url = myPostRender.resources[0].url
            val size = myPostRender.resources[0].size
            val value = if (DownloadUtils.doesLocalFileExist(url, requireContext())
                && DownloadUtils.isValidFile(url, requireContext(), size)) {
                DownloadUtils.getTemporaryFilePath(url, requireContext())} else url
            val mimeType = DownloadUtils.getMimeType(value)
            if (mimeType != null && mimeType.startsWith("video")) {
                lifecycleScope.launch(Dispatchers.IO){
                    try {
                        val urlParams = if (URLUtil.isValidUrl(value)) value else ""
                        val bitmap = FileUtils.getVideoThumbnail(value.toUri(), requireContext(), urlParams)
                        myPostRender.firstItemWidth = bitmap.intrinsicWidth
                        myPostRender.firstItemHeight = bitmap.intrinsicHeight
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            } else if(mimeType != null && mimeType.startsWith("image")){
                Glide.with(requireContext()).load(value).into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        myPostRender.firstItemWidth = resource.intrinsicWidth
                        myPostRender.firstItemHeight = resource.intrinsicHeight
                    }
                })
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        pauseVideo()
    }

    private fun calculateVisibilityVideoView(dy: Int){
        val (mainItemIndex, videoIndex) = FeedController.peekVideoQueue()
        if (mainItemIndex != null && videoIndex != null) {
            var percents = 100
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(mainItemIndex)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            val view = customGridGroup?.getChildAt(videoIndex)
            if(view is LoadingVideoView){
                view.getLocalVisibleRect(currentViewRect)
                val height = currentViewRect.height()
                if(dy >= 0){
                    if(currentViewRect.top > 0){
                        percents = (height - currentViewRect.top) * 100 /height
                    } else if(currentViewRect.bottom in 1 until height){
                        percents = currentViewRect.bottom * 100 / height
                    }
                } else {
                    percents = height * 100 / view.height
                }
                if(percents >= 30) {
                    playVideo()
                } else {
                    pauseVideo()
                }
            }
        }

    }


}