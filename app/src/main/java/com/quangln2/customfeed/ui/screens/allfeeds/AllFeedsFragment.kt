package com.quangln2.customfeed.ui.screens.allfeeds

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.quangln2.customfeed.ui.viewmodel.FeedViewModel
import com.quangln2.customfeed.ui.viewmodel.ViewModelFactory


class AllFeedsFragment : Fragment() {
    lateinit var binding: FragmentAllFeedsBinding
    lateinit var adapterVal: FeedListAdapter

    private val database by lazy {
        FeedDatabase.getFeedDatabase(requireContext())
    }
    val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }
    private val phoneStateReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Toast.makeText(context, "Phone state changed", Toast.LENGTH_SHORT).show()
            pauseVideo()
        }
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
                    scrollToPlayVideoInPosition(index, manager)
                } else if(manager is LinearLayoutManager && dy < 0) {
                    val index = manager.findFirstVisibleItemPosition()
                    scrollToPlayVideoInPosition(index, manager)
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
                listsOfPostRender.add(
                    MyPostRender.convertMyPostToMyPostRender(
                        MyPost().copy(feedId = "none"),
                        TypeOfPost.ADD_NEW_POST
                    )
                )
                it.forEach { itr -> listsOfPostRender.add(MyPostRender.convertMyPostToMyPostRender(itr)) }
                adapterVal.submitList(listsOfPostRender.toMutableList())
            } else {
                if (viewModel.feedLoadingCode.value != null) {
                    if (viewModel.feedLoadingCode.value!! != 200 && viewModel.feedLoadingCode.value!! != 0) {
                        binding.noPostId.root.visibility = View.VISIBLE
                    } else if(viewModel.feedLoadingCode.value!! == 200) {
                        val listsOfPostRender = mutableListOf<MyPostRender>()
                        listsOfPostRender.add(
                            MyPostRender.convertMyPostToMyPostRender(
                                MyPost().copy(feedId = "none"),
                                TypeOfPost.ADD_NEW_POST
                            )
                        )
                        adapterVal.submitList(listsOfPostRender.toMutableList())
                        binding.noPostId.root.visibility = View.INVISIBLE
                    }
                }
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
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
        //intent filter: PHONE STATE
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
}