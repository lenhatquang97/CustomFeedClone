package com.quangln2.customfeedui.ui.screens.allfeeds.viewholder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.databinding.FeedCardBinding
import com.quangln2.customfeedui.imageloader.domain.ImageLoader
import com.quangln2.customfeedui.others.callback.EventFeedCallback

class AddNewItemViewHolder constructor(private val binding: FeedCardBinding,
                                       private val eventFeedCallback: EventFeedCallback
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(context: Context) {
        ImageLoader.Builder().resize(100, 100).build(context).loadImage(ConstantSetup.AVATAR_LINK, binding.circleAvatar)
        binding.root.setOnClickListener {
            eventFeedCallback.onClickAddPost()
        }
    }
}