package com.quangln2.customfeedui.ui.screens.allfeeds.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.databinding.FeedHeaderBinding
import com.quangln2.customfeedui.imageloader.domain.ImageLoader
import com.quangln2.customfeedui.others.callback.EventFeedCallback
import com.quangln2.customfeedui.others.utils.FileUtils
import com.quangln2.customfeedui.uitracking.ui.UiTracking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class HeaderViewHolder constructor(
    private val binding: FeedHeaderBinding,
    private val eventFeedCallback: EventFeedCallback
    ):
    RecyclerView.ViewHolder(binding.root) {
    var imgUrl = ""
    private fun loadBasicInfoAboutFeed(item: MyPostRender, context: Context) {
        binding.feedId.text = item.feedId
        binding.myName.text = item.name
        binding.createdTime.text = FileUtils.convertUnixTimestampToTime(item.createdTime)
        imgUrl = item.avatar

        val imageLoader = ImageLoader(context, 100,100, CoroutineScope(Job()))
        imageLoader.loadImage(item.avatar, binding.myAvatarImage)
    }

    @SuppressLint("SetTextI18n")
    private fun loadFeedDescription(item: MyPostRender, context: Context) {
        binding.caption.text = item.caption
        binding.trackingInfo.text = UiTracking.formatString(item.avatar, context)
        if(item.caption.isEmpty()){
            binding.caption.visibility = View.GONE
        } else {
            binding.caption.visibility = View.VISIBLE
        }
    }

    fun bind(item: MyPostRender, context: Context){
        loadBasicInfoAboutFeed(item, context)
        loadFeedDescription(item, context)
    }

    fun onViewRecycled(){
        eventFeedCallback.onRecycled(binding.myAvatarImage)
    }
    fun onViewAttached(context: Context, countRef: Boolean = true){
        val imageLoader = ImageLoader(context, 100,100, CoroutineScope(Job()))
        imageLoader.loadImage(imgUrl, binding.myAvatarImage, countRef)
    }
}