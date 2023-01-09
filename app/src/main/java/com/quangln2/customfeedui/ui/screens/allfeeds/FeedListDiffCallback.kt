package com.quangln2.customfeedui.ui.screens.allfeeds

import androidx.recyclerview.widget.DiffUtil
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender

class FeedListDiffCallback : DiffUtil.ItemCallback<MyPostRender>() {
    override fun areItemsTheSame(oldItem: MyPostRender, newItem: MyPostRender): Boolean = oldItem == newItem
    override fun areContentsTheSame(
        oldItem: MyPostRender,
        newItem: MyPostRender
    ): Boolean {
        return oldItem.typeOfPost == newItem.typeOfPost && oldItem.feedId == newItem.feedId
                && oldItem.caption == newItem.caption && oldItem.resources == newItem.resources
                && oldItem.createdTime == newItem.createdTime
    }
}