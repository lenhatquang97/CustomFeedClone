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
import com.quangln2.customfeed.data.controllers.FeedCtrl
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
import java.util.*
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
                pauseVideoUtil()
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
                val indexLists = HashSet<Int>().apply {
                    add(firstPartiallyIndex)
                    add(firstFullIndex)
                    add(lastFullIndex)
                    add(lastPartiallyIndex)
                }.toList()
                val previousVideoDeque: Deque<Pair<Int, Int>> = LinkedList()

                for(item in FeedCtrl.videoDeque){
                    previousVideoDeque.addLast(item)
                }

                val forStack = mutableListOf<Pair<Int, Int>>()
                val forQueue = mutableListOf<Pair<Int, Int>>()

                for(i in indexLists){
                    retrieveAvailableVideos(i, forStack, forQueue)
                }

                forStack.reversed().forEach {
                    FeedCtrl.addToFirst(it.first, it.second)
                }

                forQueue.forEach {
                    FeedCtrl.addToLast(it.first, it.second)
                }

                calculateVisibilityVideoView()
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
        val (mainItemIndex, videoIndex) = FeedCtrl.peekFirst()
        if (mainItemIndex != -1 && videoIndex != -1) {
            FeedCtrl.addToFirst(mainItemIndex, videoIndex)
        }
        pauseVideoUtil()

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

    private fun pauseVideoUtil() {
        val (pausedItemIndex, videoIndex) = FeedCtrl.peekFirst()
        if (pausedItemIndex != -1 && videoIndex != -1) {
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(pausedItemIndex)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            val view = customGridGroup?.getChildAt(videoIndex)
            if (view is LoadingVideoView) {
                view.pauseAndReleaseVideo()
                FeedCtrl.popFirstSafely()
            }
        }
    }

    private fun playVideoUtil() {
        val (mainItemIndex, videoIndex) = FeedCtrl.peekFirst()
        if (mainItemIndex != -1 && videoIndex != -1) {
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(mainItemIndex)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            val view = customGridGroup?.getChildAt(videoIndex)

            if (view is LoadingVideoView) {
                view.playVideo()
                view.player.addListener(
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            super.onPlaybackStateChanged(playbackState)
                            if (playbackState == Player.STATE_ENDED) {
                                view.player.seekTo(0)
                                view.pauseAndReleaseVideo()
                                FeedCtrl.popFirstSafely()
                                calculateVisibilityVideoView()
                            }
                        }
                    }
                )
            } else {
                FeedCtrl.popFirstSafely()
            }
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
        pauseVideoUtil()
    }

    private fun checkLoadingVideoViewIsVisible(view: View): Boolean{
        if(view is LoadingVideoView){
            view.getLocalVisibleRect(currentViewRect)
            val height = currentViewRect.height()
            val isOutOfBoundsOnTheTop = currentViewRect.bottom < 0 && currentViewRect.top < 0
            val isOutOfBoundsAtTheBottom =
                currentViewRect.top >= ConstantSetup.PHONE_HEIGHT && currentViewRect.bottom >= ConstantSetup.PHONE_HEIGHT
            return if (isOutOfBoundsAtTheBottom || isOutOfBoundsOnTheTop) {
                false
            } else {
                val percents = height * 100 / view.height
                percents >= 50
            }
        }
        return false
    }

    private fun retrieveAvailableVideos(mainItemIndex: Int, forStack: MutableList<Pair<Int, Int>>, forQueue: MutableList<Pair<Int, Int>>){
        val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(mainItemIndex)
        val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
        customGridGroup?.let {
            for (i in 0 until it.size) {
                val view = it.getChildAt(i)
                val condition1 = customGridGroup.size <= 9 && i < ConstantClass.MAXIMUM_IMAGE_IN_A_GRID
                val condition2 = customGridGroup.size > 9 && i < ConstantClass.MAXIMUM_IMAGE_IN_A_GRID - 1
                if ((condition1 || condition2) && checkLoadingVideoViewIsVisible(view)) {
                    if(FeedCtrl.isEmpty()){
                        FeedCtrl.addToLast(mainItemIndex, i)
                    } else{
                        val (firstItemIndex, firstVideoIndex) = FeedCtrl.peekFirst()
                        if(firstItemIndex != -1){
                            val condition1 = mainItemIndex < firstItemIndex
                            val condition2 = mainItemIndex == firstItemIndex && i < firstVideoIndex
                            if(condition1 || condition2){
                                forStack.add(Pair(mainItemIndex, i))
                            }
                        }

                        val (lastItemIndex, lastVideoIndex) = FeedCtrl.peekLast()
                        if(lastItemIndex != -1){
                            val condition1 = mainItemIndex > lastItemIndex
                            val condition2 = mainItemIndex == lastItemIndex && i > lastVideoIndex
                            if(condition1 || condition2){
                                forQueue.add(Pair(mainItemIndex, i))
                            }
                        }
                    }

                }
            }

        }
    }


    private fun calculateVisibilityVideoView() {
        val (mainItemIndex, videoIndex) = FeedCtrl.peekFirst()
        if (mainItemIndex != -1 && videoIndex != -1) {
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(mainItemIndex)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            val view = customGridGroup?.getChildAt(videoIndex)
            view?.let {
                if (it is LoadingVideoView){
                     if(checkLoadingVideoViewIsVisible(it)){
                         checkWhetherAllVideosArePlayingExceptMain(mainItemIndex, videoIndex)
                         playVideoUtil()
                    } else pauseVideoUtil()
                }
            }
        }
    }

    private fun checkWhetherAllVideosArePlayingExceptMain(first: Int, second: Int){
        for (item in FeedCtrl.videoDeque){
            val (mainItemIndex, videoIndex) = item
            val condition2 = mainItemIndex == first && videoIndex == second
            if(condition2) continue
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(mainItemIndex)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            val view = customGridGroup?.getChildAt(videoIndex)
            if(view is LoadingVideoView && view.player.isPlaying){
                view.pauseAndReleaseVideo()
            }
        }
    }

}