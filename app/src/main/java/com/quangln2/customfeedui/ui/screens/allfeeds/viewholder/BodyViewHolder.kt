package com.quangln2.customfeedui.ui.screens.allfeeds.viewholder
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.models.uimodel.CurrentVideo
import com.quangln2.customfeedui.data.models.uimodel.ItemLocation
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.databinding.FeedBodyBinding
import com.quangln2.customfeedui.imageloader.data.network.NetworkHelper
import com.quangln2.customfeedui.imageloader.domain.ImageLoader
import com.quangln2.customfeedui.others.callback.EventFeedCallback
import com.quangln2.customfeedui.others.utils.DownloadUtils
import com.quangln2.customfeedui.ui.customview.CustomLayer
import com.quangln2.customfeedui.ui.customview.LoadingVideoView
import com.quangln2.customfeedui.ui.customview.customgrid.CustomGridGroup
import com.quangln2.customfeedui.uitracking.UiTracking
import kotlinx.coroutines.*
import java.io.File
class BodyViewHolder constructor(private val binding: FeedBodyBinding,
                                 private var context: Context,
                                 private val eventFeedCallback: EventFeedCallback):
    RecyclerView.ViewHolder(binding.root) {
    private var gridForLayout = mutableListOf<ItemLocation>()

    @SuppressLint("SetTextI18n")
    private fun addBlackLayerForViewMore(i: Int, item: MyPostRender): Boolean {
        if (i >= 8 && item.resources.size > ConstantSetup.MAXIMUM_IMAGE_IN_A_GRID) {
            val numbersOfAddedImages = item.resources.size - ConstantSetup.MAXIMUM_IMAGE_IN_A_GRID + 1
            val viewChild = CustomLayer(context)
            viewChild.setOnClickListener {
                eventFeedCallback.onClickViewMore(item.feedId)
            }
            viewChild.addedImagesText.text = "+$numbersOfAddedImages"
            binding.customGridGroup.addView(viewChild)
            return true
        }
        return false
    }

    private fun afterLoad(item: MyPostRender) {
        fun getFileUri(url: String): String{
            val actualUrl = NetworkHelper.convertVideoUrlToImageUrl(url)
            val fileName = URLUtil.guessFileName(actualUrl, null, null)
            val file = File(context.cacheDir, fileName)
            return file.toUri().toString()
        }

        val keyList = item.resources.map {getFileUri(it.url) }.toList()
        binding.trackingInfo.text = UiTracking.getAllImageReferences(keyList)
    }

    fun bind(item: MyPostRender){
        binding.customGridGroup.removeAllViews()
        binding.customGridGroup.firstItemWidth = item.firstItemWidth
        binding.customGridGroup.firstItemHeight = item.firstItemHeight
        gridForLayout.clear()
        gridForLayout.addAll(CustomGridGroup.initializeDataForShowingGrid(item.resources.size, item.firstItemWidth, item.firstItemHeight))


        for (i in item.resources.indices) {
            if (addBlackLayerForViewMore(i, item)) return
            val url = item.resources[i].url
            val mimeType = DownloadUtils.getMimeType(url)
            val urlArrayList = ArrayList<String>().apply {
                item.resources.forEach {
                    add(it.url)
                }
            }

            if (mimeType != null && mimeType.contains("video")) {
                createVideoView(item, i, url, urlArrayList)
            } else {
                createImageView(item, i, url, urlArrayList)
            }
        }
        afterLoad(item)
    }

    private fun createVideoView(item: MyPostRender, i: Int, url: String, urlArrayList: ArrayList<String>){
        val currentVideo = CurrentVideo(currentVideoPosition = -1L, url = item.resources[i].url, listOfUrls = urlArrayList, id = item.feedId)
        val videoView = LoadingVideoView(context, url)
        videoView.initForShowThumbnail(gridForLayout[i].width, gridForLayout[i].height)
        videoView.setOnClickListener {
            eventFeedCallback.onClickVideoView(currentVideo)
        }
        binding.customGridGroup.addView(videoView)
    }
    private fun createImageView(item: MyPostRender, i: Int, url: String, urlArrayList: ArrayList<String>){
        val currentVideo = CurrentVideo(currentVideoPosition = -1L, url = item.resources[i].url, listOfUrls = urlArrayList, id = item.feedId)
        val imageView = ImageView(context).apply {
            background = ColorDrawable(Color.parseColor("#aaaaaa"))
            scaleType = ImageView.ScaleType.CENTER_CROP
            setOnClickListener {
                eventFeedCallback.onClickVideoView(currentVideo)
            }
        }
        binding.customGridGroup.addView(imageView)
        ImageLoader.Builder()
            .resize(gridForLayout[i].width, gridForLayout[i].height)
            .putIntoFolder(item.feedId)
            .build(context)
            .loadImage(url, imageView)
    }
}