package com.quangln2.customfeedui.ui.screens.allfeeds.viewholder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.databinding.FeedCardBinding
import com.quangln2.customfeedui.others.callback.EventFeedCallback

class AddNewItemViewHolder constructor(private val binding: FeedCardBinding,
                                       private val eventFeedCallback: EventFeedCallback
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(context: Context) {
        Glide.with(context).load(ConstantSetup.AVATAR_LINK).apply(ConstantSetup.REQUEST_OPTIONS_WITH_SIZE_100).into(binding.circleAvatar)
        binding.root.setOnClickListener {
            eventFeedCallback.onClickAddPost()
        }
    }
}