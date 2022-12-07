package com.quangln2.customfeedui.ui.screens.allfeeds

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.data.models.uimodel.TypeOfPost
import com.quangln2.customfeedui.databinding.FeedBodyBinding
import com.quangln2.customfeedui.databinding.FeedCardBinding
import com.quangln2.customfeedui.databinding.FeedFooterBinding
import com.quangln2.customfeedui.databinding.FeedHeaderBinding
import com.quangln2.customfeedui.others.callback.EventFeedCallback
import com.quangln2.customfeedui.ui.screens.allfeeds.viewholder.AddNewItemViewHolder
import com.quangln2.customfeedui.ui.screens.allfeeds.viewholder.BodyViewHolder
import com.quangln2.customfeedui.ui.screens.allfeeds.viewholder.FooterViewHolder
import com.quangln2.customfeedui.ui.screens.allfeeds.viewholder.HeaderViewHolder
import java.util.concurrent.Executors

class FeedListAdapter(
    private var context: Context,
    private val eventFeedCallback: EventFeedCallback
) :
    androidx.recyclerview.widget.ListAdapter<MyPostRender, RecyclerView.ViewHolder>(
        AsyncDifferConfig.Builder(FeedListDiffCallback())
            .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
            .build()
    ) {

    override fun getItemViewType(position: Int): Int {
        val item = currentList[position]
        return item.typeOfPost.value
    }

    override fun getItemId(position: Int): Long {
        val id = currentList[position].feedId
        return id.hashCode().toLong()
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType){
            TypeOfPost.ADD_NEW_POST.value -> {
                val binding = FeedCardBinding.inflate(layoutInflater, parent, false)
                AddNewItemViewHolder(binding, eventFeedCallback)
            }
            TypeOfPost.HEADER.value -> {
                val binding = FeedHeaderBinding.inflate(layoutInflater, parent, false)
                HeaderViewHolder(binding, eventFeedCallback)
            }
            TypeOfPost.BODY.value -> {
                val binding = FeedBodyBinding.inflate(layoutInflater, parent, false)
                BodyViewHolder(binding, context, eventFeedCallback)
            }
            else -> {
                val binding = FeedFooterBinding.inflate(layoutInflater, parent, false)
                FooterViewHolder(binding, eventFeedCallback)
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        when (holder) {
            is BodyViewHolder -> {
                holder.onViewRecycled()
            }
            is HeaderViewHolder -> {
                holder.onViewRecycled()
            }
            is AddNewItemViewHolder -> {
                holder.onViewRecycled()
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when(getItemViewType(position)){
            TypeOfPost.ADD_NEW_POST.value -> (holder as AddNewItemViewHolder).bind(context)
            TypeOfPost.HEADER.value -> (holder as HeaderViewHolder).bind(item, context)
            TypeOfPost.BODY.value -> (holder as BodyViewHolder).bind(item, context)
            else -> (holder as FooterViewHolder).bind(item)

        }
    }

}

class FeedListDiffCallback : DiffUtil.ItemCallback<MyPostRender>() {
    override fun areItemsTheSame(oldItem: MyPostRender, newItem: MyPostRender): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: MyPostRender,
        newItem: MyPostRender
    ): Boolean {
        return oldItem.typeOfPost == newItem.typeOfPost && oldItem.feedId == newItem.feedId
                && oldItem.caption == newItem.caption && oldItem.resources == newItem.resources
                && oldItem.createdTime == newItem.createdTime
    }
}

