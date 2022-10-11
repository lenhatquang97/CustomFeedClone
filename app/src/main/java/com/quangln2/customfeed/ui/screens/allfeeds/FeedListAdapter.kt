package com.quangln2.customfeed.ui.screens.allfeeds

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.size
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.constants.ConstantClass
import com.quangln2.customfeed.data.models.uimodel.MyPostRender
import com.quangln2.customfeed.data.models.uimodel.TypeOfPost
import com.quangln2.customfeed.databinding.FeedCardBinding
import com.quangln2.customfeed.databinding.FeedItemBinding
import com.quangln2.customfeed.others.callback.EventFeedCallback
import com.quangln2.customfeed.others.utils.DownloadUtils
import com.quangln2.customfeed.others.utils.FileUtils
import com.quangln2.customfeed.ui.customview.CustomGridGroup
import com.quangln2.customfeed.ui.customview.CustomLayer
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class FeedListAdapter(
    private var context: Context,
    private val eventFeedCallback: EventFeedCallback
) :
    ListAdapter<MyPostRender, RecyclerView.ViewHolder>(
        AsyncDifferConfig.Builder(FeedListDiffCallback())
            .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
            .build()
    ) {

    inner class AddNewItemViewHolder constructor(private val binding: FeedCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context) {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(100)
            Glide.with(context).load(ConstantClass.AVATAR_LINK).apply(requestOptions).into(binding.circleAvatar)
            binding.root.setOnClickListener {
                eventFeedCallback.onClickAddPost()
            }
        }
    }

    inner class FeedItemViewHolder constructor(private val binding: FeedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyPostRender, context: Context) {
            binding.loadingCircularIndicator.visibility = View.VISIBLE
            if (binding.customGridGroup.size > 0) {
                binding.loadingCircularIndicator.visibility = View.GONE
                //return
            }
            println("Bind item in ${item.feedId} ${item.caption}")

            binding.feedId.text = item.feedId
            binding.myName.text = item.name

            println("Bind item out ${item.feedId} ${item.caption}")

            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(200)
            Glide.with(context).load(item.avatar).apply(requestOptions).into(binding.myAvatarImage)
            binding.createdTime.text = FileUtils.convertUnixTimestampToTime(item.createdTime)
            if (item.caption.isEmpty()) {
                binding.caption.visibility = View.GONE
            } else {
                binding.caption.visibility = View.VISIBLE
                if (item.caption.length > 50) {
                    binding.caption.text = item.caption.substring(0, 50) + "..."
                    binding.learnMore.visibility = View.VISIBLE
                    binding.learnLess.visibility = View.GONE
                } else {
                    binding.caption.text = item.caption
                    binding.learnMore.visibility = View.GONE
                    binding.learnLess.visibility = View.GONE
                }
            }

            binding.deleteButton.setOnClickListener {
                val position = adapterPosition
                val itr = currentList[position]

                eventFeedCallback.onDeleteItem(itr.feedId)
            }

            binding.learnMore.setOnClickListener {
                binding.caption.text = item.caption
                binding.learnMore.visibility = View.GONE
                binding.learnLess.visibility = View.VISIBLE
            }
            binding.learnLess.setOnClickListener {
                binding.caption.text = item.caption.substring(0, 50) + "..."
                binding.learnMore.visibility = View.VISIBLE
                binding.learnLess.visibility = View.GONE
            }
            if (item.resources.size == 0) {
                binding.loadingCircularIndicator.visibility = View.GONE
                binding.customGridGroup.visibility = View.GONE
            }

            CoroutineScope(Dispatchers.IO).launch {
                for (i in 0 until item.resources.size) {
                    if (i >= 8 && item.resources.size > 9) {
                        val numbersOfAddedImages = item.resources.size - 9
                        val viewChild = CustomLayer(context)
                        viewChild.setOnClickListener {
                            eventFeedCallback.onClickViewMore(item.feedId)
                        }
                        viewChild.addedImagesText.text = "+$numbersOfAddedImages"
                        withContext(Dispatchers.Main) {
                            binding.customGridGroup.addView(viewChild)
                        }
                        break
                    }

                    val value = if (DownloadUtils.doesLocalFileExist(
                            item.resources[i].url,
                            context
                        ) && DownloadUtils.isValidFile(item.resources[i].url, context, item.resources[i].size)
                    ) {
                        DownloadUtils.getTemporaryFilePath(item.resources[i].url, context)
                    } else {
                        item.resources[i].url
                    }
                    if (value.contains("mp4")) {
                        withContext(Dispatchers.Main) {
                            val videoView = LoadingVideoView(context, value)
                            videoView.layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            videoView.setOnClickListener {
                                println("Click video ${item.resources.map { it.url }.toList()}")
                                val stringArr = ArrayList<String>()
                                item.resources.forEach {
                                    stringArr.add(it.url)
                                }
                                eventFeedCallback.onClickVideoView(
                                    item.resources[i].url,
                                    stringArr
                                )
                            }
                            binding.loadingCircularIndicator.visibility = View.INVISIBLE
                            binding.customGridGroup.addView(videoView)
                        }
                    } else {
                        val imageView = ImageView(context)
                        imageView.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                        imageView.setOnClickListener {
                            eventFeedCallback.onClickVideoView(
                                item.resources[i].url,
                                item.resources.map { it.url }.toList() as ArrayList<String>
                            )
                        }

                        withContext(Dispatchers.Main) {
                            Glide.with(context).load(value).listener(
                                object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        binding.loadingCircularIndicator.visibility = View.VISIBLE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        binding.loadingCircularIndicator.visibility = View.INVISIBLE
                                        return false
                                    }
                                }
                            ).apply(requestOptions).into(object : SimpleTarget<Drawable>() {
                                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                    binding.customGridGroup.firstWidth = resource.intrinsicWidth
                                    binding.customGridGroup.firstHeight = resource.intrinsicHeight
                                    imageView.setImageDrawable(resource)
                                }

                            })
                            binding.customGridGroup.addView(imageView)
                        }
                    }
                }

            }


        }


    }


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
        if (viewType == TypeOfPost.ADD_NEW_POST.value) {
            val binding = FeedCardBinding.inflate(layoutInflater, parent, false)
            return this.AddNewItemViewHolder(binding)
        } else {
            val binding = FeedItemBinding.inflate(layoutInflater, parent, false)
            return this.FeedItemViewHolder(binding)
        }

    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is FeedItemViewHolder) {
            val customGridGroup = holder.itemView.findViewById<CustomGridGroup>(R.id.customGridGroup)
            for(i in 0 until customGridGroup.size) {
                val child = customGridGroup.getChildAt(i)
                if (child is LoadingVideoView) {
                    child.player.pause()
                    child.player.release()
                }
            }
            customGridGroup.removeAllViews()
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = getItem(position)
        val itemType = getItemViewType(position)
        if (itemType == TypeOfPost.ADD_NEW_POST.value) {
            (holder as AddNewItemViewHolder).bind(context)
        } else {
            (holder as FeedItemViewHolder).bind(item.copy(), context)
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
        return oldItem.typeOfPost == newItem.typeOfPost && oldItem.feedId == newItem.feedId && oldItem.caption == newItem.caption && oldItem.resources == newItem.resources && oldItem.createdTime == newItem.createdTime
    }


}
