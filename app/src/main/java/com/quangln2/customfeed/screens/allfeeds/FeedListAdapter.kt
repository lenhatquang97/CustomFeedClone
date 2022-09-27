package com.quangln2.customfeed.screens

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
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.quangln2.customfeed.constants.ConstantClass
import com.quangln2.customfeed.customview.CustomLayer
import com.quangln2.customfeed.customview.LoadingVideoView
import com.quangln2.customfeed.databinding.FeedCardBinding
import com.quangln2.customfeed.databinding.FeedItemBinding
import com.quangln2.customfeed.models.UploadPost
import com.quangln2.customfeed.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class FeedListAdapter(
    private var context: Context,
    private val onDeleteItem: (String) -> Unit,
    private val onClickAddPost: () -> Unit,
    private val onClickVideoView: (String) -> Unit,
    private val onClickViewMore: () -> Unit
) :
    ListAdapter<UploadPost, RecyclerView.ViewHolder>(
        AsyncDifferConfig.Builder(FeedListDiffCallback())
            .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
            .build()
    ) {

    inner class AddNewItemViewHolder constructor(private val binding: FeedCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context) {
            Glide.with(context).load(ConstantClass.AVATAR_LINK).into(binding.circleAvatar)
            binding.root.setOnClickListener {
                onClickAddPost()
            }
        }
    }

    inner class FeedItemViewHolder constructor(private val binding: FeedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UploadPost, context: Context) {
            binding.loadingCircularIndicator.visibility = View.VISIBLE
            if (binding.customGridGroup.size > 0) {
                binding.loadingCircularIndicator.visibility = View.GONE
                return
            }

            binding.feedId.text = item.feedId
            binding.myName.text = item.name
            Glide.with(context).load(item.avatar).into(binding.myAvatarImage)
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
                onDeleteItem(item.feedId)
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

            CoroutineScope(Dispatchers.IO).launch {
                for (i in 0 until item.imagesAndVideos.size) {
                    if (i >= 8 && item.imagesAndVideos.size > 9) {
                        val numbersOfAddedImages = item.imagesAndVideos.size - 9
                        val viewChild = CustomLayer(context)
                        viewChild.setOnClickListener {
                            onClickViewMore()
                        }
                        viewChild.addedImagesText.text = "+$numbersOfAddedImages"
                        withContext(Dispatchers.Main) {
                            binding.customGridGroup.addView(viewChild)
                        }
                        break
                    }

                    val value = item.imagesAndVideos[i]
                    if (value.contains("mp4")) {
                        withContext(Dispatchers.Main) {
                            val videoView = LoadingVideoView(context, value)
                            videoView.layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            videoView.setOnClickListener {
                                onClickVideoView(value)
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
                            onClickVideoView(value)
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
                            ).into(object : SimpleTarget<Drawable>() {
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
        return position
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        if (viewType == 0) {
            val binding = FeedCardBinding.inflate(layoutInflater, parent, false)
            return this.AddNewItemViewHolder(binding)
        } else {
            val binding = FeedItemBinding.inflate(layoutInflater, parent, false)
            return this.FeedItemViewHolder(binding)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            (holder as FeedListAdapter.AddNewItemViewHolder).bind(context)
        } else {
            val item = getItem(position)
            (holder as FeedListAdapter.FeedItemViewHolder).bind(item, context)
        }
    }


    override fun getItemId(position: Int): Long {
        return getItem(position).feedId.hashCode().toLong()
    }


}

class FeedListDiffCallback : DiffUtil.ItemCallback<UploadPost>() {
    override fun areItemsTheSame(oldItem: UploadPost, newItem: UploadPost): Boolean {
        return oldItem.feedId == newItem.feedId
    }

    override fun areContentsTheSame(
        oldItem: UploadPost,
        newItem: UploadPost
    ): Boolean {
        if (oldItem.imagesAndVideos.size != newItem.imagesAndVideos.size) {
            return false
        }
        for (i in 0 until oldItem.imagesAndVideos.size) {
            if (oldItem.imagesAndVideos[i] != newItem.imagesAndVideos[i]) {
                return false
            }
        }
        return oldItem.feedId == newItem.feedId
    }

}
