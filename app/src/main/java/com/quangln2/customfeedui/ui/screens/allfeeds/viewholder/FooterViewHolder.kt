package com.quangln2.customfeedui.ui.screens.allfeeds.viewholder

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.databinding.FeedFooterBinding
import com.quangln2.customfeedui.others.callback.EventFeedCallback

class FooterViewHolder constructor(private val binding: FeedFooterBinding,
                                   private val eventFeedCallback: EventFeedCallback):
    RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    private fun bindingButton(item: MyPostRender) {
        binding.deleteButton.setOnClickListener {
            eventFeedCallback.onDeleteItem(item.feedId, position)
        }
    }
    fun bind(item: MyPostRender, context: Context){
        bindingButton(item)
    }
}