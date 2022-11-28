package com.quangln2.customfeedui.ui.screens.allfeeds

import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.controllers.FeedCtrl
import com.quangln2.customfeedui.data.database.FeedDatabase
import com.quangln2.customfeedui.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeedui.data.models.others.EnumFeedLoadingCode
import com.quangln2.customfeedui.data.models.others.EnumFeedSplashScreenState
import com.quangln2.customfeedui.data.models.uimodel.CurrentVideo
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.databinding.FragmentAllFeedsBinding
import com.quangln2.customfeedui.others.callback.EventFeedCallback
import com.quangln2.customfeedui.others.utils.FileUtils
import com.quangln2.customfeedui.ui.customview.LoadingVideoView
import com.quangln2.customfeedui.ui.viewmodel.FeedViewModel
import com.quangln2.customfeedui.ui.viewmodel.ViewModelFactory
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
    private val player by lazy { ExoPlayer.Builder(requireContext()).build() }

    private var feedVideoItemPlaying = Pair(-1, -1)

    private val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }

    private val playerListener: Player.Listener get() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            viewModel.onPlaybackStateEnded(
                playbackState = playbackState,
                onEndPlayVideo =  {onEndPlayVideo(player)},
                onPlayVideo = { playVideoUtil() })
        }
    }

    private val eventCallback: EventFeedCallback
        get() = object : EventFeedCallback {
            override fun onDeleteItem(id: String, position: Int) {
                viewModel.deleteFeed(id, requireContext())
                positionDeletedOrRefreshed.set(position)
            }

            override fun onClickAddPost(){
                val permissionCheck = ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                if(permissionCheck == PackageManager.PERMISSION_GRANTED){
                    findNavController().navigate(R.id.action_allFeedsFragment_to_homeScreenFragment, null, navOptions {
                        anim {
                            enter = android.R.animator.fade_in
                            exit = android.R.animator.fade_out
                        }
                    })
                } else if(shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                    FileUtils.getPermissionForStorageWithMultipleTimesDenial(requireContext())
                } else{
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                }
            }


            override fun onClickVideoView(currentVideo: CurrentVideo) =
                findNavController().navigate(
                    R.id.action_allFeedsFragment_to_viewFullVideoFragment,
                    currentVideo.encapsulateToBundle(),
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

            override fun onRecycled(child: View) {
                if(child is LoadingVideoView){
                    child.pauseAndReleaseVideo(player)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.startUploadService(requireContext())
        viewModel.getAllFeeds(preloadCache = true)
        player.addListener(playerListener)
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
                    viewModel.onHandlePlayVideoAndDownloadVideo(firstVisibleItemPosition, requireContext()){
                        if(!isVideoPlaying())
                            playVideoUtil()
                    }
                }
            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val manager = recyclerView.layoutManager as LinearLayoutManager
                val tmpFirstVisibleItemPosition = manager.findFirstVisibleItemPosition()
                val firstPartiallyIndex = if(tmpFirstVisibleItemPosition < 0) 0 else tmpFirstVisibleItemPosition
                val lastPartiallyIndex = manager.findLastVisibleItemPosition()

                viewModel.retrieveAllVisibleItems(firstPartiallyIndex, lastPartiallyIndex){
                    clearVideoDeque()
                    retrieveAllVisibleVideosOnScreen(it)
                }

                //Step 2: put incoming video to play
                viewModel.putIncomingVideoIntoQueueWrapper {
                    if(FeedCtrl.playingQueue.isNotEmpty()){
                        val (pausedItemIndex, videoIndex) = FeedCtrl.playingQueue.remove()
                        pauseVideoUtil(pausedItemIndex, videoIndex)
                    }
                }

                if(binding.allFeeds.scrollState == RecyclerView.SCROLL_STATE_IDLE){
                    playVideoUtil()
                }
            }
        })

        viewModel.feedLoadingCode.observe(viewLifecycleOwner){
            val allDefinedCodes = listOf(-1, 0, 200)
            when (it) {
                !in allDefinedCodes -> {
                    binding.noPostId.apply {
                        imageView.visibility = View.GONE
                        textNote.visibility = View.GONE
                    }
                    binding.retryButton.visibility = View.VISIBLE
                    binding.swipeRefreshLayout.isRefreshing = false
                }
                EnumFeedLoadingCode.INITIAL.value -> {
                    binding.noPostId.root.visibility = View.VISIBLE
                }
            }
        }

        viewModel.uploadLists.observe(viewLifecycleOwner) {
            it?.apply {
                binding.noPostId.root.visibility = View.INVISIBLE
                binding.retryButton.visibility = View.GONE
                lifecycleScope.launch(Dispatchers.IO) {
                    val listsOfPostRender = MyPostRender.convertToListWithRenderedPost(it)
                    withContext(Dispatchers.Main) {
                        //Handle whether to have loading screen or not
                        val feedLoadingCode = viewModel.feedLoadingCode.value
                        val emptyFeedCondition = listsOfPostRender.size == 1 && feedLoadingCode == EnumFeedLoadingCode.SUCCESS.value
                        val havePostCondition = listsOfPostRender.size > 1
                        if(emptyFeedCondition || havePostCondition){
                            binding.noPostId.root.visibility = View.GONE
                            binding.swipeRefreshLayout.isRefreshing = false
                            binding.retryButton.visibility = View.GONE
                            binding.allFeeds.visibility = View.VISIBLE
                        } else {
                            binding.noPostId.root.visibility = View.VISIBLE
                            binding.allFeeds.visibility = View.GONE
                        }

                        //only submitList
                        adapterVal.submitList(listsOfPostRender.toMutableList()){
                            if(positionDeletedOrRefreshed.get() >= 1){
                                binding.allFeeds.scrollToPosition(positionDeletedOrRefreshed.get() - 1)
                                positionDeletedOrRefreshed.set(-1)
                            }
                        }
                    }
                }
            }

        }



        FeedCtrl.isLoadingToUpload.observe(viewLifecycleOwner) {
            binding.loadingCard.root.visibility = when(it){
                EnumFeedSplashScreenState.LOADING.value -> View.VISIBLE
                else -> View.GONE
            }
            if (it == EnumFeedSplashScreenState.COMPLETE.value) {
                //UI State
                binding.swipeRefreshLayout.isRefreshing = true
                binding.noPostId.imageView.visibility = View.VISIBLE
                binding.noPostId.textNote.text = resources.getString(R.string.loading)

                //Code state
                FeedCtrl.isLoadingToUpload.value = EnumFeedSplashScreenState.UNDEFINED.value

                viewModel.getAllFeeds()
                viewModel.stopUploadService(requireContext())
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.retryButton.setOnClickListener {
            binding.noPostId.imageView.visibility = View.VISIBLE
            binding.noPostId.textNote.visibility = View.VISIBLE
            binding.retryButton.visibility = View.GONE

            viewModel.getAllFeeds(preloadCache = true)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.noPostId.imageView.visibility = View.VISIBLE
            binding.noPostId.textNote.text = resources.getString(R.string.loading)

            positionDeletedOrRefreshed.set(1)
            viewModel.getAllFeeds{
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        playVideoUtil()
    }

    override fun onPause() {
        super.onPause()
        if(FeedCtrl.playingQueue.isNotEmpty()){
            val (pausedItemIndex, videoIndex) = FeedCtrl.playingQueue.peek()!!
            pauseVideoUtil(pausedItemIndex, videoIndex)
        }
    }

    private fun isVideoPlaying(): Boolean {
        val (pausedItemIndex, videoIndex) = feedVideoItemPlaying
        val view = getVideoView(pausedItemIndex, videoIndex)
        if (view is LoadingVideoView) {
            return view.playerView.player?.isPlaying ?: false
        }
        return false
    }

    private fun pauseVideoUtil(pausedItemIndex: Int, videoIndex: Int) {
        val view = getVideoView(pausedItemIndex, videoIndex)
        if (view is LoadingVideoView) {
            view.pauseAndReleaseVideo(player)
        }
    }

    private fun playVideoUtil() {
        if(FeedCtrl.playingQueue.isNotEmpty()){
            val (mainItemIndex, videoIndex) = FeedCtrl.playingQueue.peek()!!
            val view = getVideoView(mainItemIndex, videoIndex)

            if (view != null && view is LoadingVideoView) {
                feedVideoItemPlaying = Pair(mainItemIndex, videoIndex)
                view.playVideo(player)
            }
        }

    }
    private fun onEndPlayVideo(player: ExoPlayer){
        val (mainItemIndex, videoIndex) = feedVideoItemPlaying
        val view = getVideoView(mainItemIndex, videoIndex)
        if(view != null && view is LoadingVideoView){
            view.onEndPlayVideo(player)
        }
    }

    private fun clearVideoDeque() = FeedCtrl.videoDeque.clear()

    private fun retrieveAllVisibleVideosOnScreen(visibleFeeds: List<Int>){
        visibleFeeds.forEach { feedIdx ->
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(feedIdx)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            customGridGroup?.let{
                for(i in 0 until it.size){
                    val view = it.getChildAt(i)
                    val videoPair = Pair(feedIdx, i)
                    viewModel.checkLoadingVideoViewIsVisible(view, currentViewRect, videoPair)
                }
            }
        }
    }

    private fun getVideoView(mainItemIndex: Int, videoIndex: Int): View?{
        val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(mainItemIndex)
        val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
        return customGridGroup?.getChildAt(videoIndex)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.removeListener(playerListener)
        player.release()
        FeedCtrl.isLoadingToUpload.value?.apply {
            when(this){
                EnumFeedSplashScreenState.COMPLETE.value,
                EnumFeedSplashScreenState.UNDEFINED.value -> {
                    viewModel.stopUploadService(requireContext())
                }
            }
        }
    }
}