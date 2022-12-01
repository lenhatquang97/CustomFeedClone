package com.quangln2.customfeedui.ui.screens.allfeeds.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.databinding.FeedHeaderBinding
import com.quangln2.customfeedui.others.utils.FileUtils

class HeaderViewHolder constructor(private val binding: FeedHeaderBinding):
    RecyclerView.ViewHolder(binding.root) {
    private fun loadBasicInfoAboutFeed(item: MyPostRender, context: Context) {
        binding.feedId.text = item.feedId
        binding.myName.text = item.name
        binding.createdTime.text = FileUtils.convertUnixTimestampToTime(item.createdTime)
        Glide.with(context)
            .load(item.avatar)
            .apply(ConstantSetup.REQUEST_OPTIONS_WITH_SIZE_100)
            .into(binding.myAvatarImage)
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