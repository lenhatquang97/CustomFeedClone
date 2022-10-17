package com.quangln2.customfeed.ui.screens.allfeeds

import android.os.Bundle
import androidx.core.view.size
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.Player
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.controllers.FeedController
import com.quangln2.customfeed.data.controllers.VideoPlayed
import com.quangln2.customfeed.others.callback.EventFeedCallback
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.ui.customview.customgrid.CustomGridGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


fun AllFeedsFragment.scrollToPlayVideoInPosition(itemPosition: Int, linearLayoutManager: LinearLayoutManager) {
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


fun checkWhetherHaveMoreThanTwoVideosInPost(): Boolean {
    val (mainItemIndex, videoIndex) = FeedController.popVideoQueue()
    val (anotherMainItemIndex, anotherVideoIndex) = FeedController.popVideoQueue()
    if (mainItemIndex != null && videoIndex != null && anotherMainItemIndex != null && anotherVideoIndex != null) {
        FeedController.videoQueue.add(VideoPlayed(mainItemIndex, videoIndex))
        FeedController.videoQueue.add(VideoPlayed(anotherMainItemIndex, anotherVideoIndex))
        if (mainItemIndex == anotherMainItemIndex) return true
    }
    return false
}

fun AllFeedsFragment.pauseVideo() {
    val (pausedItemIndex, videoIndex) = FeedController.peekVideoQueue()
     lifecycleScope.launch(Dispatchers.Main) {
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


fun AllFeedsFragment.playVideo(){
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

val AllFeedsFragment.eventCallback: EventFeedCallback get() = object : EventFeedCallback {
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