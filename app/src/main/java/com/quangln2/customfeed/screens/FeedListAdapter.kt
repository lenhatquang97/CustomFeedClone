package com.quangln2.customfeed.screens

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.size
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Util
import com.quangln2.customfeed.R
import com.quangln2.customfeed.constants.ConstantClass
import com.quangln2.customfeed.customview.CustomLayer
import com.quangln2.customfeed.databinding.FeedCardBinding
import com.quangln2.customfeed.databinding.FeedItemBinding
import com.quangln2.customfeed.models.UploadPost
import com.quangln2.customfeed.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.Executors

class FeedListAdapter(
    private var context: Context,
    private val onDeleteItem: (String) -> Unit,
    private val onClickAddPost: () -> Unit,
    private val onClickVideoView: (String) -> Unit
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
            if(binding.customGridGroup.size > 0){
                binding.loadingCircularIndicator.visibility = View.GONE
                return
            }

            binding.feedId.text = item.feedId
            binding.myName.text = item.name
            Glide.with(context).load(item.avatar).into(binding.myAvatarImage)
            binding.createdTime.text = FileUtils.convertUnixTimestampToTime(item.createdTime)
            binding.caption.text = item.caption

            binding.deleteButton.setOnClickListener {
                onDeleteItem(item.feedId)
            }

            println("Created ${item.createdTime}")

            CoroutineScope(Dispatchers.IO).launch {
                for (i in 0 until item.imagesAndVideos.size) {
                    if (i >= 8 && item.imagesAndVideos.size > 9) {
                        val numbersOfAddedImages = item.imagesAndVideos.size - 9
                        val viewChild = CustomLayer(context)
                        viewChild.textValue = "+$numbersOfAddedImages"
                        withContext(Dispatchers.Main) {
                            binding.customGridGroup.addView(viewChild)
                        }
                        break
                    }

                    val value = item.imagesAndVideos[i]
                    if (value.contains("mp4")) {
                        withContext(Dispatchers.IO) {
                            val videoView = VideoView(context)
                            videoView.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            val uri = Uri.parse(value)
                            val mediaController = MediaController(context)
                            mediaController.visibility = View.INVISIBLE
                            videoView.setMediaController(mediaController)

                            videoView.setVideoURI(uri)
                            videoView.requestFocus()

                            videoView.setOnClickListener {
                                onClickVideoView(value)
                            }




                            videoView.setBackgroundDrawable(context.resources.getDrawable(R.drawable.played))
                            withContext(Dispatchers.Main){
                                binding.customGridGroup.addView(videoView)
                                binding.loadingCircularIndicator.visibility = View.INVISIBLE
                            }


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
                            ).into(imageView)
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
            (holder as AddNewItemViewHolder).bind(context)
        } else {
            val item = getItem(position)
            (holder as FeedItemViewHolder).bind(item, context)
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
