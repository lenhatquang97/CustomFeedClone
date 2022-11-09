package com.quangln2.customfeed.ui.screens.allfeeds

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.constants.ConstantSetup
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
    private lateinit var player: ExoPlayer
    var feedVideoItemPlaying = Pair(-1, -1)

    val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }

    private val temporaryVideoSequence = mutableListOf<Pair<Int, Int>>()

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
        player = ExoPlayer.Builder(requireContext()).build()
        viewModel.getAllFeedsWithPreloadCache()
        intializePlayerListener()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllFeedsBinding.inflate(inflater, container, false)
        adapterVal = FeedListAdapter(requireContext(), eventCallback, player)
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
                    if(!isVideoPlaying()){
                        playVideoUtil()
                    }

                    //Download video resource
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
                }.filter { it >= 0 }.toList()

                //Step 1: Get all visible items
                retrieveAllVisibleVideosOnScreen(indexLists)

                //Step 2: put incoming video to play
                putIncomingVideoToQueue()

                //Case when first load into fragment
                if(binding.allFeeds.scrollState == RecyclerView.SCROLL_STATE_IDLE){
                    playVideoUtil()
                }
            }
        })

        viewModel.feedLoadingCode.observe(viewLifecycleOwner){
            val allDefinedCodes = listOf(-1, 0, 200)
            if(it !in allDefinedCodes){
                binding.noPostId.apply {
                    imageView.visibility = View.GONE
                    textNote.visibility = View.GONE
                }
                binding.retryButton.visibility = View.VISIBLE
            } else if(it != EnumFeedLoadingCode.SUCCESS.value && it != EnumFeedLoadingCode.INITIAL.value){
                binding.noPostId.root.visibility = View.VISIBLE
            } else if(it == EnumFeedLoadingCode.SUCCESS.value){
                binding.noPostId.root.visibility = View.GONE
            }
        }

        viewModel.uploadLists.observe(viewLifecycleOwner) {
            it?.apply {
                binding.noPostId.root.visibility = View.INVISIBLE
                binding.retryButton.visibility = View.GONE
                lifecycleScope.launch(Dispatchers.IO) {
                    val listsOfPostRender = mutableListOf<MyPostRender>()
                    val addNewPostItem = MyPostRender.convertMyPostToMyPostRender(MyPost(), TypeOfPost.ADD_NEW_POST)
                    listsOfPostRender.add(addNewPostItem)
                    it.forEach { itr ->
                        val myPostRender = MyPostRender.convertMyPostToMyPostRender(itr)
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
            }

        }

        binding.retryButton.setOnClickListener {
            binding.noPostId.imageView.visibility = View.VISIBLE
            binding.noPostId.textNote.visibility = View.VISIBLE
            binding.retryButton.visibility = View.GONE
            viewModel.getAllFeedsWithPreloadCache()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.noPostId.imageView.visibility = View.VISIBLE
            binding.noPostId.textNote.text = resources.getString(R.string.loading)
            positionDeletedOrRefreshed.set(1)
            val onNotChangedData = fun(){
                binding.swipeRefreshLayout.isRefreshing = false
            }
            viewModel.getAllFeeds(onNotChangedData)

        }

        FeedCtrl.isLoadingToUpload.observe(viewLifecycleOwner) {
            binding.loadingCard.root.visibility = if (it == EnumFeedSplashScreenState.LOADING.value) View.VISIBLE else View.GONE
            if (it == EnumFeedSplashScreenState.COMPLETE.value) {
                binding.swipeRefreshLayout.post {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                binding.noPostId.imageView.visibility = View.VISIBLE
                binding.noPostId.textNote.text = resources.getString(R.string.loading)
                viewModel.getAllFeeds()
                FeedCtrl.isLoadingToUpload.value = EnumFeedSplashScreenState.UNDEFINED.value
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        playVideoUtil()
    }

    override fun onPause() {
        super.onPause()
        pauseVideoWithoutPop()
    }

    private fun pauseVideoUtil() {
        if(FeedCtrl.playingQueue.isEmpty()) return
        val (pausedItemIndex, videoIndex) = FeedCtrl.playingQueue.remove()
        val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(pausedItemIndex)
        val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
        val view = customGridGroup?.getChildAt(videoIndex)
        if (view is LoadingVideoView) {
            view.pauseAndReleaseVideo(player)
        }
    }

    private fun pauseVideoWithoutPop() {
        if(FeedCtrl.playingQueue.isEmpty()) return
        val (pausedItemIndex, videoIndex) = FeedCtrl.playingQueue.peek()!!
        val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(pausedItemIndex)
        val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
        val view = customGridGroup?.getChildAt(videoIndex)
        if (view is LoadingVideoView) {
            view.pauseAndReleaseVideo(player)
        }
    }
    private fun isVideoPlaying(): Boolean {
        if(FeedCtrl.playingQueue.isEmpty()) return false
        val (pausedItemIndex, videoIndex) = FeedCtrl.playingQueue.peek()!!
        val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(pausedItemIndex)
        val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
        val view = customGridGroup?.getChildAt(videoIndex)
        if (view is LoadingVideoView) {
            return player.isPlaying
        }
        return false
    }



    private fun playVideoUtil() {
        if(FeedCtrl.playingQueue.isNotEmpty()){
            val (mainItemIndex, videoIndex) = FeedCtrl.playingQueue.peek()!!
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(mainItemIndex)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            val view = customGridGroup?.getChildAt(videoIndex)


            if (view is LoadingVideoView) {
                feedVideoItemPlaying = Pair(mainItemIndex, videoIndex)
                view.playVideo(player)
            }
        }

    }
    private fun intializePlayerListener(){
        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)

                    val (mainItemIndex, videoIndex) = feedVideoItemPlaying
                    if (playbackState == Player.STATE_ENDED) {
                        player.release()
                        player = ExoPlayer.Builder(requireContext()).build()
                        player.addListener(this)
                        if(FeedCtrl.videoDeque.isNotEmpty()){
                            FeedCtrl.playingQueue.remove()
                            val hasDuplicatedVideo = FeedCtrl.videoDeque.filter { it.first == mainItemIndex && it.second == videoIndex }
                            if(hasDuplicatedVideo.isNotEmpty()){
                                while(FeedCtrl.videoDeque.isNotEmpty()){
                                    val pair = FeedCtrl.videoDeque.remove()
                                    if(pair.first == mainItemIndex && pair.second == videoIndex){
                                        break
                                    }
                                }
                            }
                            val pair = FeedCtrl.videoDeque.peek()
                            if(pair != null){
                                FeedCtrl.playingQueue.add(pair)
                                playVideoUtil()
                            }
                        }
                    }
                }
            }
        )
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
                val tmp = view.height
                if(tmp == 0) false
                val percents = height * 100 / tmp
                percents >= 50
            }
        }
        return false
    }

    private fun retrieveAllVisibleVideosOnScreen(visibleFeeds: List<Int>){
        FeedCtrl.videoDeque.clear()
        visibleFeeds.forEach { feedIdx ->
            val viewItem = binding.allFeeds.findViewHolderForAdapterPosition(feedIdx)
            val customGridGroup = viewItem?.itemView?.findViewById<FrameLayout>(R.id.customGridGroup)
            customGridGroup?.let{
                for(i in 0 until it.size){
                    val view = it.getChildAt(i)
                    if (checkLoadingVideoViewIsVisible(view)) {
                        FeedCtrl.addToLast(feedIdx, i)
                    }
                }
            }
        }
    }

    private fun putIncomingVideoToQueue(){
        Log.v("FeedFragment", "Compare two arrays $temporaryVideoSequence ${FeedCtrl.videoDeque}")
        if(!FeedCtrl.compareDequeWithList(temporaryVideoSequence)){
            if(temporaryVideoSequence.isEmpty()){
                temporaryVideoSequence.addAll(FeedCtrl.videoDeque)
            } else {
                temporaryVideoSequence.clear()
                temporaryVideoSequence.addAll(FeedCtrl.videoDeque)
            }


            val pair = FeedCtrl.peekFirst()
            if(pair.first != -1 && pair.second != -1){
                //Case we have video in queue
                if(FeedCtrl.playingQueue.isEmpty()){
                    //Case 1: No video available in playing queue
                    FeedCtrl.popFirstSafely()
                    FeedCtrl.playingQueue.add(pair)
                } else {
                    //Case 2: We have video in playing queue
                    val (itemIndex, videoIndex) = FeedCtrl.playingQueue.peek()!!
                    if(itemIndex == pair.first && videoIndex == pair.second){
                        //Case 2.1: The video in playing queue is the same as the video in queue
                        FeedCtrl.popFirstSafely()
                    } else {
                        //Case 2.2: The video in playing queue is different from the video in queue
                        Log.v("FeedFragment", "-----------------------")
                        Log.v("FeedFragment", "Different ${FeedCtrl.playingQueue} ${FeedCtrl.videoDeque}")
                        pauseVideoUtil()
                        FeedCtrl.popFirstSafely()
                        FeedCtrl.playingQueue.clear()
                        FeedCtrl.playingQueue.add(pair)
                        Log.v("FeedFragment", "Different ${FeedCtrl.playingQueue} ${FeedCtrl.videoDeque}")
                        Log.v("FeedFragment", "-----------------------")
                        temporaryVideoSequence.clear()
                    }
                }
            } else {
                //Case no video in feeds
                while(FeedCtrl.playingQueue.isNotEmpty()){
                    pauseVideoUtil()
                    FeedCtrl.popFirstSafely()
                }
                temporaryVideoSequence.clear()
            }
        }
    }


}