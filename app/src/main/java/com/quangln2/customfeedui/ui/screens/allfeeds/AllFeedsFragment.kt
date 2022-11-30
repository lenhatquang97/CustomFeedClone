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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.quangln2.customfeedui.data.models.others.EnumFeedSplashScreenState
import com.quangln2.customfeedui.data.models.uimodel.CurrentVideo
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.databinding.FragmentAllFeedsBinding
import com.quangln2.customfeedui.others.callback.EventFeedCallback
import com.quangln2.customfeedui.others.utils.FileUtils
import com.quangln2.customfeedui.ui.customview.LoadingVideoView
import com.quangln2.customfeedui.ui.viewmodel.FeedViewModel
import com.quangln2.customfeedui.ui.viewmodelfactory.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger


class AllFeedsFragment : Fragment() {
    private lateinit var binding: FragmentAllFeedsBinding
    private lateinit var adapterVal: FeedListAdapter
    private var feedVideoItemPlaying = Pair(-1, -1)

    private val database by lazy { FeedDatabase.getFeedDatabase(requireContext()) }
    private val currentViewRect by lazy { Rect() }
    private val positionDeletedOrRefreshed by lazy { AtomicInteger(-1) }
    private val player by lazy { ExoPlayer.Builder(requireContext()).build() }
    private val navTransition by lazy {
        navOptions {
            anim {
                enter = android.R.animator.fade_in
                exit = android.R.animator.fade_out
            }
        }
    }

    private val viewModel: FeedViewModel by viewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }

    private val playerListener: Player.Listener get() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            lifecycleScope.launch(Dispatchers.Main){
                viewModel.onPlaybackStateEnded(playbackState).collect{
                    when(it){
                        "onEndPlayVideo" -> onEndPlayVideo(player)
                        "onPlayVideo" -> playVideoUtil()
                    }
                }
            }
        }
    }

    private val eventCallback: EventFeedCallback get() = object : EventFeedCallback {
            override fun onDeleteItem(id: String, position: Int) {
                viewModel.deleteFeed(id, requireContext())
                positionDeletedOrRefreshed.set(position)
            }
            override fun onClickAddPost(){
                val permissionCheck = ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                if(permissionCheck == PackageManager.PERMISSION_GRANTED){
                    findNavController().navigate(R.id.action_allFeedsFragment_to_homeScreenFragment, null, navTransition)
                } else if(shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                    FileUtils.getPermissionForStorageWithMultipleTimesDenial(requireContext())
                } else{
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                }
            }
            override fun onClickVideoView(currentVideo: CurrentVideo) =
                findNavController().navigate(R.id.action_allFeedsFragment_to_viewFullVideoFragment, currentVideo.encapsulateToBundle(), navTransition)
            override fun onClickViewMore(id: String) = findNavController().
                navigate(R.id.action_allFeedsFragment_to_viewMoreFragment, Bundle().apply { putString("id", id) }, navTransition)
            override fun onRecycled(child: View) {
                if(child is LoadingVideoView){
                    child.pauseAndReleaseVideo(player)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.startUploadService(requireContext())
        lifecycleScope.launch(Dispatchers.Main){
            viewModel.getAllFeeds(preloadCache = true)
        }
        player.addListener(playerListener)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Initialize data binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_feeds, container,false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        //Initialize recycler view
        adapterVal = FeedListAdapter(requireContext(), eventCallback)
        adapterVal.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.allFeeds.apply {
            adapter = adapterVal
            layoutManager = LinearLayoutManager(requireContext())
            animation = null
        }
        binding.allFeeds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val firstVisibleItemPosition = (binding.allFeeds.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    lifecycleScope.launch(Dispatchers.Main){
                        viewModel.onHandlePlayVideoAndDownloadVideo(firstVisibleItemPosition, requireContext()).collect{
                            when(it){
                                "playVideoWrapper" -> {
                                    if(!isVideoPlaying())
                                        playVideoUtil()
                                }
                            }
                        }
                    }
                }
            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val manager = recyclerView.layoutManager as LinearLayoutManager
                val firstPartiallyIndex = manager.findFirstVisibleItemPosition()
                val lastPartiallyIndex = manager.findLastVisibleItemPosition()
                val indexLists = viewModel.retrieveAllVisibleItems(firstPartiallyIndex, lastPartiallyIndex)

                //Step 1: Clear deque and get visible items
                clearVideoDeque()
                retrieveAllVisibleVideosOnScreen(indexLists)
                //Step 2: put incoming video to play
                lifecycleScope.launch(Dispatchers.Main){
                    viewModel.putIncomingVideoToQueue().collect{
                        when(it){
                            "pauseVideoUtilCustom" -> {
                                if(FeedCtrl.playingQueue.isNotEmpty()){
                                    val (pausedItemIndex, videoIndex) = FeedCtrl.playingQueue.remove()
                                    pauseVideoUtil(pausedItemIndex, videoIndex)
                                }
                            }
                        }
                    }
                }
            }
        })

        //Handle observe uploadLists
        viewModel.uploadLists.observe(viewLifecycleOwner) {
            it?.apply {
                val listOfPostRender = MyPostRender.convertToListWithRenderedPost(it)
                viewModel.manageUploadList(listOfPostRender)
                adapterVal.submitList(listOfPostRender.toMutableList()){
                    if(positionDeletedOrRefreshed.get() >= 1){
                        binding.allFeeds.scrollToPosition(positionDeletedOrRefreshed.get() - 1)
                        positionDeletedOrRefreshed.set(-1)
                    }
                }
            }
        }
        //Handle uploading
        FeedCtrl.isLoadingToUpload.observe(viewLifecycleOwner) {
            it?.apply {
                viewModel.manageUploadState(it, requireContext())
            }

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.setOnRefreshListener {
            positionDeletedOrRefreshed.set(1)
            viewModel.onHandleSwipeRefresh()
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
}