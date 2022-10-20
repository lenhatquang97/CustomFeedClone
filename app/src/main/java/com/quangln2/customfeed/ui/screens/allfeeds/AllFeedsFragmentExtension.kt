package com.quangln2.customfeed.ui.screens.allfeeds

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.FrameLayout
import androidx.core.net.toUri
import androidx.core.view.size
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.constants.ConstantClass
import com.quangln2.customfeed.data.controllers.FeedController
import com.quangln2.customfeed.data.controllers.VideoPlayed
import com.quangln2.customfeed.data.models.uimodel.MyPostRender
import com.quangln2.customfeed.others.callback.EventFeedCallback
import com.quangln2.customfeed.others.utils.DownloadUtils
import com.quangln2.customfeed.others.utils.FileUtils
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


fun AllFeedsFragment.scrollToPlayVideoInPosition(itemPosition: Int, linearLayoutManager: LinearLayoutManager) {
    val viewItem = linearLayoutManager.findViewByPosition(itemPosition)
    val firstCondition = (itemPosition > 0 && viewItem != null)
    val secondCondition = (viewItem != null && itemPosition == adapterVal.itemCount - 1)

    if (firstCondition || secondCondition) {
        val customGridGroup = viewItem?.findViewById<FrameLayout>(R.id.customGridGroup)
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
    }
}


fun checkWhetherHaveMoreThanTwoVideosInPost(): Boolean {
    val (mainItemIndex, videoIndex) = FeedController.popVideoQueue()
    val (anotherMainItemIndex, anotherVideoIndex) = FeedController.popVideoQueue()
    if (mainItemIndex != null && videoIndex != null && anotherMainItemIndex != null && anotherVideoIndex != null) {
        FeedController.videoQueue.add(VideoPlayed(mainItemIndex, videoIndex))
        //FeedController.videoQueue.add(VideoPlayed(anotherMainItemIndex, anotherVideoIndex))
        if (mainItemIndex == anotherMainItemIndex) return true
    }
    return false
}

fun AllFeedsFragment.pauseVideo() {
    val (pausedItemIndex, videoIndex) = FeedController.peekVideoQueue()
    lifecycleScope.launch(Dispatchers.Main) {
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

}


fun AllFeedsFragment.playVideo(){
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
                        if (playbackState == Player.STATE_ENDED) {
                            //End this video
                            view.player.seekTo(0)
                            view.pauseVideo()
                            FeedController.safeRemoveFromQueue()

                            //Play next video in list
                            for (i in videoIndex until customGridGroup.size) {
                                val nextView = customGridGroup.getChildAt(i)
                                if (nextView is LoadingVideoView && i != videoIndex && i < ConstantClass.MAXIMUM_IMAGE_IN_A_GRID) {
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

fun AllFeedsFragment.retrieveFirstImageOrFirstVideo(myPostRender: MyPostRender){
    if(myPostRender.resources.size > 0){
        val url = myPostRender.resources[0].url
        val size = myPostRender.resources[0].size
        val value = if (DownloadUtils.doesLocalFileExist(url, requireContext())
            && DownloadUtils.isValidFile(url, requireContext(), size)) {
            DownloadUtils.getTemporaryFilePath(url, requireContext())} else url
        val mimeType = DownloadUtils.getMimeType(value)
        if (mimeType != null && mimeType.startsWith("video")) {
            try {
                val urlParams = if (URLUtil.isValidUrl(value)) value else ""
                val bitmap = FileUtils.getVideoThumbnail(value.toUri(), requireContext(), urlParams)
                myPostRender.firstItemWidth = bitmap.intrinsicWidth
                myPostRender.firstItemHeight = bitmap.intrinsicHeight
            } catch (e: Exception) {
                e.printStackTrace()
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