package com.quangln2.customfeedui.ui.screens.allfeeds.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.databinding.FeedHeaderBinding
import com.quangln2.customfeedui.imageloader.domain.ImageLoader
import com.quangln2.customfeedui.others.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class HeaderViewHolder constructor(private val binding: FeedHeaderBinding):
    RecyclerView.ViewHolder(binding.root) {
    private fun loadBasicInfoAboutFeed(item: MyPostRender, context: Context) {
        binding.feedId.text = item.feedId
        binding.myName.text = item.name
        binding.createdTime.text = FileUtils.convertUnixTimestampToTime(item.createdTime)

        val imageLoader = ImageLoader(context, 100,100, CoroutineScope(Job()))
        imageLoader.loadImage(item.avatar, binding.myAvatarImage)
    }

    @SuppressLint("SetTextI18n")
    private fun loadFeedDescription(item: MyPostRender) {
        binding.caption.text = item.caption
        if(item.caption.isEmpty()){
            binding.caption.visibility = View.GONE
        } else {
            binding.caption.visibility = View.VISIBLE
        }
    }

    fun bind(item: MyPostRender, context: Context){
        loadBasicInfoAboutFeed(item, context)
        loadFeedDescription(item)
    }
}