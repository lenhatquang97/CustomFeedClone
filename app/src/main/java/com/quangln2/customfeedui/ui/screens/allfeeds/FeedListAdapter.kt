package com.quangln2.customfeedui.ui.screens.allfeeds

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.size
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.ExoPlayer
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.data.models.uimodel.RectanglePoint
import com.quangln2.customfeedui.data.models.uimodel.TypeOfPost
import com.quangln2.customfeedui.databinding.FeedCardBinding
import com.quangln2.customfeedui.databinding.FeedItemBinding
import com.quangln2.customfeedui.others.callback.EventFeedCallback
import com.quangln2.customfeedui.others.utils.DownloadUtils
import com.quangln2.customfeedui.others.utils.DownloadUtils.getMimeType
import com.quangln2.customfeedui.others.utils.FileUtils
import com.quangln2.customfeedui.ui.customview.CustomLayer
import com.quangln2.customfeedui.ui.customview.LoadingVideoView
import com.quangln2.customfeedui.ui.customview.customgrid.getGridItemsLocation
import java.util.concurrent.Executors

class FeedListAdapter(
    private var context: Context,
    private val eventFeedCallback: EventFeedCallback,
    private val player: ExoPlayer
) :
    androidx.recyclerview.widget.ListAdapter<MyPostRender, RecyclerView.ViewHolder>(
        AsyncDifferConfig.Builder(FeedListDiffCallback())
            .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
            .build()
    ) {

    inner class AddNewItemViewHolder constructor(private val binding: FeedCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context) {
            Glide.with(context).load(ConstantSetup.AVATAR_LINK).apply(ConstantSetup.REQUEST_OPTIONS_WITH_SIZE_100).into(binding.circleAvatar)
            binding.root.setOnClickListener {
                eventFeedCallback.onClickAddPost()
            }
        }
    }

    inner class FeedItemViewHolder constructor(private val binding: FeedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun afterLoad(item: MyPostRender) {
            if (item.resources.size == 0) {
                binding.customGridGroup.visibility = View.GONE
            } else {
                binding.customGridGroup.visibility = View.VISIBLE
            }
        }

        private fun loadBasicInfoAboutFeed(item: MyPostRender) {
            binding.feedId.text = item.feedId
            binding.myName.text = item.name
            binding.createdTime.text = FileUtils.convertUnixTimestampToTime(item.createdTime)
            Glide.with(context).load(item.avatar).apply(ConstantSetup.REQUEST_OPTIONS_WITH_SIZE_100)
                .into(binding.myAvatarImage)
        }

        @SuppressLint("SetTextI18n")
        private fun loadFeedDescription(item: MyPostRender) {
            if (item.caption.isEmpty()) {
                binding.caption.visibility = View.GONE
                binding.learnMore.visibility = View.GONE
                binding.learnLess.visibility = View.GONE
            } else {
                binding.caption.visibility = View.VISIBLE
                if (item.caption.length > 50) {
                    binding.caption.text = "${item.caption.substring(0, 50)}..."
                    binding.learnMore.visibility = View.VISIBLE
                    binding.learnLess.visibility = View.GONE
                } else {
                    binding.caption.text = item.caption
                    binding.learnMore.visibility = View.GONE
                    binding.learnLess.visibility = View.GONE
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun bindingButton(item: MyPostRender) {
            binding.deleteButton.setOnClickListener {
                val itr = currentList[adapterPosition]
                eventFeedCallback.onDeleteItem(itr.feedId, position)
            }

            binding.learnMore.setOnClickListener {
                binding.caption.text = item.caption
                binding.learnMore.visibility = View.GONE
                binding.learnLess.visibility = View.VISIBLE
            }

            binding.learnLess.setOnClickListener {
                binding.caption.text = "${item.caption.substring(0, 50)}..."
                binding.learnMore.visibility = View.VISIBLE
                binding.learnLess.visibility = View.GONE
            }
        }

        @SuppressLint("SetTextI18n")
        private fun addMoreImageOrVideoLayer(
            i: Int,
            item: MyPostRender,
            rectangles: List<RectanglePoint>,
            widthGrid: Int,
            contentPadding: Int
        ): Boolean {
            if (i >= 8 && item.resources.size > ConstantSetup.MAXIMUM_IMAGE_IN_A_GRID) {
                val numbersOfAddedImages = item.resources.size - ConstantSetup.MAXIMUM_IMAGE_IN_A_GRID
                val viewChild = CustomLayer(context)

                val leftView = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                val topView = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
                val widthView = (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding
                val heightView = (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding

                val layoutParams = ViewGroup.MarginLayoutParams(widthView, heightView).apply {
                    leftMargin = leftView
                    topMargin = topView
                }

                viewChild.layoutParams = layoutParams
                viewChild.setOnClickListener {
                    eventFeedCallback.onClickViewMore(item.feedId)
                }
                viewChild.addedImagesText.text = "+$numbersOfAddedImages"
                binding.customGridGroup.addView(viewChild)
                return true
            }
            return false
        }

        fun bind(item: MyPostRender, context: Context) {
            val rectangles = getGridItemsLocation(item.resources.size, item.firstItemWidth, item.firstItemHeight)
            val contentPadding = 16
            val marginHorizontalSum = 16 + 32
            val widthGrid = Resources.getSystem().displayMetrics.widthPixels - marginHorizontalSum
//            val renderersFactory = DefaultRenderersFactory(context).forceEnableMediaCodecAsynchronousQueueing()


            loadBasicInfoAboutFeed(item)
            loadFeedDescription(item)
            bindingButton(item)
            if (rectangles.isNotEmpty()) {
                for (i in rectangles.indices) {
                    if (addMoreImageOrVideoLayer(i, item, rectangles, widthGrid, contentPadding)) return
                    val url = item.resources[i].url
                    val doesLocalFileExist = DownloadUtils.doesLocalFileExist(url, context)
                    val isValidFile = DownloadUtils.isValidFile(url, context, item.resources[i].size)
                    val temporaryFilePath = DownloadUtils.getTemporaryFilePath(url, context)
                    val value = if (doesLocalFileExist && isValidFile) temporaryFilePath else url
                    val mimeType = getMimeType(value)


                    val leftView = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                    val topView = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
                    val widthView = (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding
                    val heightView = (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding

                    if (mimeType != null && mimeType.contains("video")) {
                        val videoView = LoadingVideoView(context, value)
                        val layoutParams = ViewGroup.MarginLayoutParams(widthView, heightView).apply {
                            leftMargin = leftView
                            topMargin = topView
                        }
                        videoView.layoutParams = layoutParams
                        videoView.setOnClickListener {
                            val stringArr = ArrayList<String>()
                            item.resources.forEach {
                                stringArr.add(it.url)
                            }
                            eventFeedCallback.onClickVideoView(
                                videoView.currentPosition,
                                item.resources[i].url,
                                stringArr
                            )
                        }

                        binding.customGridGroup.addView(videoView)
                    } else {
                        val imageView = ImageView(context)
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                        val layoutParams = ViewGroup.MarginLayoutParams(widthView, heightView).apply {
                            leftMargin = leftView
                            topMargin = topView
                        }
                        imageView.layoutParams = layoutParams

                        imageView.setOnClickListener {
                            val urlArrayList = ArrayList<String>()
                            item.resources.forEach { urlArrayList.add(it.url) }
                            eventFeedCallback.onClickVideoView(-1L, item.resources[i].url, urlArrayList)
                        }

                        Glide.with(context).load(value).apply(
                           ConstantSetup.REQUEST_WITH_RGB_565
                        ).placeholder(ColorDrawable(Color.parseColor("#aaaaaa")))
                            .listener(
                                object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        val drawable = ColorDrawable(Color.parseColor("#aaaaaa"))
                                        imageView.setImageDrawable(drawable)
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        dataSource: com.bumptech.glide.load.DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        return false
                                    }
                                }
                            ).centerCrop().into(imageView)
                        binding.customGridGroup.addView(imageView)
                    }
                }
            }
            afterLoad(item)
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
        return if (viewType == TypeOfPost.ADD_NEW_POST.value) {
            val binding = FeedCardBinding.inflate(layoutInflater, parent, false)
            this.AddNewItemViewHolder(binding)
        } else {
            val binding = FeedItemBinding.inflate(layoutInflater, parent, false)
            this.FeedItemViewHolder(binding)
        }

    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        Log.v("FeedAdapter", "onViewRecycled")
        if (holder is FeedItemViewHolder) {
            val customGridGroup = holder.itemView.findViewById<FrameLayout>(R.id.customGridGroup)
            for (i in 0 until customGridGroup.size) {
                val child = customGridGroup.getChildAt(i)
                if (child is LoadingVideoView) {
                    child.pauseAndReleaseVideo(player)
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
            (holder as FeedItemViewHolder).bind(item, context)
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

