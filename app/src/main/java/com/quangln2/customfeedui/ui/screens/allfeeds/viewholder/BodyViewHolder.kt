package com.quangln2.customfeedui.ui.screens.allfeeds.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.models.uimodel.CurrentVideo
import com.quangln2.customfeedui.data.models.uimodel.ItemLocation
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.databinding.FeedBodyBinding
import com.quangln2.customfeedui.imageloader.data.bitmap.BitmapCustomParams
import com.quangln2.customfeedui.imageloader.data.network.NetworkHelper
import com.quangln2.customfeedui.imageloader.domain.ImageLoader
import com.quangln2.customfeedui.others.callback.EventFeedCallback
import com.quangln2.customfeedui.others.utils.DownloadUtils
import com.quangln2.customfeedui.ui.customview.CustomLayer
import com.quangln2.customfeedui.ui.customview.LoadingVideoView
import com.quangln2.customfeedui.ui.customview.customgrid.getGridItemsLocation
import com.quangln2.customfeedui.uitracking.ui.UiTracking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.io.File

class BodyViewHolder constructor(private val binding: FeedBodyBinding,
                                 private var context: Context,
                                 private val eventFeedCallback: EventFeedCallback):
    RecyclerView.ViewHolder(binding.root) {
    private var itemUseForAttached: MyPostRender? = null
    private val listOfImageUrl = mutableListOf<String>()
    private fun initializeDataForShowingGrid(item: MyPostRender): List<ItemLocation>{
        val rectangles = getGridItemsLocation(item.resources.size, item.firstItemWidth, item.firstItemHeight)
        val contentPadding = 16
        val marginHorizontalSum = 16 + 32
        val widthGrid = Resources.getSystem().displayMetrics.widthPixels - marginHorizontalSum
        val itemLocations = mutableListOf<ItemLocation>()
        for(i in rectangles.indices){
            val leftView = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
            val topView = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
            val widthView = (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding
            val heightView = (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
            val itemLocation = ItemLocation(leftView, topView, widthView, heightView)
            itemLocations.add(itemLocation)
        }
        return itemLocations
    }

    @SuppressLint("SetTextI18n")
    private fun addMoreImageOrVideoLayer(i: Int, item: MyPostRender, rectangles: List<ItemLocation>): Boolean {
        if (i >= 8 && item.resources.size > ConstantSetup.MAXIMUM_IMAGE_IN_A_GRID) {
            val numbersOfAddedImages = item.resources.size - ConstantSetup.MAXIMUM_IMAGE_IN_A_GRID
            val viewChild = CustomLayer(context)

            val layoutParamsCustom = ViewGroup.MarginLayoutParams(rectangles[i].width, rectangles[i].height).apply {
                leftMargin = rectangles[i].left
                topMargin = rectangles[i].top
            }

            viewChild.layoutParams = layoutParamsCustom
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

    fun bind(item: MyPostRender, context: Context){
        listOfImageUrl.clear()
        itemUseForAttached = item
        val rectangles = initializeDataForShowingGrid(item)
        if (rectangles.isNotEmpty()) {
            for (i in rectangles.indices) {
                if (addMoreImageOrVideoLayer(i, item, rectangles)) return
                val url = item.resources[i].url
                val mimeType = DownloadUtils.getMimeType(url)

                val layoutParamsCustom = ViewGroup.MarginLayoutParams(rectangles[i].width, rectangles[i].height).apply {
                    leftMargin = rectangles[i].left
                    topMargin = rectangles[i].top
                }
                val urlArrayList = ArrayList<String>().apply {
                    item.resources.forEach {
                        add(it.url)
                    }
                }

                //Usage: Use for recycling
                item.resources.forEach {
                    listOfImageUrl.add(it.url)
                }

                val currentVideo = CurrentVideo(currentVideoPosition = -1L, url = item.resources[i].url, listOfUrls = urlArrayList, id = item.feedId)
                if (mimeType != null && mimeType.contains("video")) {
                    val videoView = LoadingVideoView(context, url, rectangles[i].width, rectangles[i].height)
                    videoView.layoutParams = layoutParamsCustom
                    videoView.setOnClickListener {
                        eventFeedCallback.onClickVideoView(currentVideo)
                    }
                    binding.customGridGroup.addView(videoView)
                } else {
                    val imageView = ImageView(context).apply {
                        background = ColorDrawable(Color.parseColor("#aaaaaa"))
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        layoutParams = layoutParamsCustom
                        setOnClickListener {
                            eventFeedCallback.onClickVideoView(currentVideo)
                        }
                    }
                    val imageLoader = ImageLoader(context, rectangles[i].width, rectangles[i].height, CoroutineScope(Job()))
                    val bmpParams = BitmapCustomParams().apply { folderName = item.feedId }
                    imageLoader.loadImage(url, imageView, bmpParams)
                    binding.customGridGroup.addView(imageView)
                }
            }
        }
        afterLoad(item)
    }

    fun onViewRecycled(){
        for (i in 0 until binding.customGridGroup.size) {
            val child = binding.customGridGroup.getChildAt(i)
            eventFeedCallback.onRecycled(child)
        }
        binding.customGridGroup.removeAllViews()
    }

    fun onViewAttached(){
        binding.customGridGroup.removeAllViews()
        itemUseForAttached?.let { bind(it, context) }
    }
}