package com.quangln2.customfeedui.ui.screens.allfeeds.viewholder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.databinding.FeedCardBinding
import com.quangln2.customfeedui.imageloader.domain.ImageLoader
import com.quangln2.customfeedui.others.callback.EventFeedCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class AddNewItemViewHolder constructor(private val binding: FeedCardBinding,
                                       private val eventFeedCallback: EventFeedCallback
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(context: Context) {
        val imageLoader = ImageLoader(context,100,100, CoroutineScope(Job()))
        imageLoader.loadImage(ConstantSetup.AVATAR_LINK, binding.circleAvatar)
        binding.root.setOnClickListener {
            eventFeedCallback.onClickAddPost()
        }
    }
    fun onViewRecycled(){
        eventFeedCallback.onRecycled(binding.circleAvatar)
    }
}