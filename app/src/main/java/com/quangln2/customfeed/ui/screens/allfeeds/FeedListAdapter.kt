package com.quangln2.customfeed.ui.screens.allfeeds

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.constants.ConstantClass
import com.quangln2.customfeed.data.controllers.FeedController
import com.quangln2.customfeed.data.models.uimodel.MyPostRender
import com.quangln2.customfeed.data.models.uimodel.RectanglePoint
import com.quangln2.customfeed.data.models.uimodel.TypeOfPost
import com.quangln2.customfeed.databinding.FeedCardBinding
import com.quangln2.customfeed.databinding.FeedItemBinding
import com.quangln2.customfeed.others.callback.EventFeedCallback
import com.quangln2.customfeed.others.utils.DownloadUtils
import com.quangln2.customfeed.others.utils.DownloadUtils.getMimeType
import com.quangln2.customfeed.others.utils.FileUtils
import com.quangln2.customfeed.ui.customview.CustomLayer
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.ui.customview.customgrid.getGridItemsLocation
import java.util.concurrent.Executors

class FeedListAdapter(
    private var context: Context,
    private val eventFeedCallback: EventFeedCallback
) :
    androidx.recyclerview.widget.ListAdapter<MyPostRender, RecyclerView.ViewHolder>(
        AsyncDifferConfig.Builder<MyPostRender>(FeedListDiffCallback())
            .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
            .build()
    ) {

    inner class AddNewItemViewHolder constructor(private val binding: FeedCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context) {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(50)
            Glide.with(context).load(ConstantClass.AVATAR_LINK).apply(requestOptions).into(binding.circleAvatar)
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
            Glide.with(context).load(item.avatar).apply(ConstantClass.REQUEST_OPTIONS_WITH_SIZE_100)
                .into(binding.myAvatarImage)
        }

        @SuppressLint("SetTextI18n")
        private fun loadFeedDescription(item: MyPostRender) {
            if (item.caption.isEmpty()) {
                binding.caption.visibility = View.GONE
            } else {
                binding.caption.visibility = View.VISIBLE
                if (item.caption.length > 50) {
                    binding.caption.text =  "${item.caption.substring(0, 50)}..."
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
                eventFeedCallback.onDeleteItem(itr.feedId)
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
        private fun addMoreImageOrVideoLayer(i: Int, item: MyPostRender, rectangles: List<RectanglePoint>, widthGrid: Int, contentPadding: Int): Boolean {
            if (i >= 8 && item.resources.size > ConstantClass.MAXIMUM_IMAGE_IN_A_GRID) {
                val numbersOfAddedImages = item.resources.size - ConstantClass.MAXIMUM_IMAGE_IN_A_GRID
                val viewChild = CustomLayer(context)

                val layoutParams = ViewGroup.MarginLayoutParams(
                    (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding,
                    (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
                ).apply {
                    leftMargin = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                    topMargin = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
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
            val marginHorizontalSum = 16 + 32
            val widthGrid = Resources.getSystem().displayMetrics.widthPixels - marginHorizontalSum
            val contentPadding = 16


            loadBasicInfoAboutFeed(item)
            loadFeedDescription(item)
            bindingButton(item)
            if(rectangles.isNotEmpty()){
                for (i in 0 until item.resources.size) {
                    if (addMoreImageOrVideoLayer(i, item, rectangles, widthGrid, contentPadding)) return
                    val doesLocalFileExist = DownloadUtils.doesLocalFileExist(item.resources[i].url, context)
                    val isValidFile = DownloadUtils.isValidFile(item.resources[i].url, context, item.resources[i].size)
                    val temporaryFilePath = DownloadUtils.getTemporaryFilePath(item.resources[i].url, context)
                    val url = item.resources[i].url

                    val value = if (doesLocalFileExist && isValidFile) temporaryFilePath else url
                    val mimeType = getMimeType(value)

                    if (mimeType != null && mimeType.contains("video")) {
                        val videoView = LoadingVideoView(context, value)
                        val layoutParams = ViewGroup.MarginLayoutParams(
                            (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding,
                            (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
                        ).apply {
                            leftMargin = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                            topMargin = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
                        }
                        videoView.layoutParams = layoutParams
                        videoView.setOnClickListener {
                            val stringArr = ArrayList<String>()
                            item.resources.forEach {
                                stringArr.add(it.url)
                            }
                            eventFeedCallback.onClickVideoView(
                                videoView.player.currentPosition,
                                item.resources[i].url,
                                stringArr
                            )
                        }
                        binding.customGridGroup.addView(videoView)
                    } else {
                        val imageView = ImageView(context)
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                        val layoutParams = ViewGroup.MarginLayoutParams(
                            (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding,
                            (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
                        ).apply {
                            leftMargin = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                            topMargin = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
                        }
                        imageView.layoutParams = layoutParams

                        imageView.setOnClickListener {
                            val urlArrayList = ArrayList<String>()
                            item.resources.forEach { urlArrayList.add(it.url) }
                            eventFeedCallback.onClickVideoView(-1L, item.resources[i].url, urlArrayList)
                        }

                        when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                val drawable = ColorDrawable(Color.parseColor("#aaaaaa"))
                                imageView.setImageDrawable(drawable)
                            }
                            Configuration.UI_MODE_NIGHT_NO -> {
                                val drawable = ContextCompat.getDrawable(context, R.drawable.placeholder_image)
                                imageView.setImageDrawable(drawable)
                            }
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                val drawable = ColorDrawable(Color.parseColor("#aaaaaa"))
                                imageView.setImageDrawable(drawable)
                            }
                        }



                        Glide.with(context).load(value).listener(
                            object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                                        Configuration.UI_MODE_NIGHT_YES -> {
                                            val drawable = ColorDrawable(Color.parseColor("#aaaaaa"))
                                            imageView.setImageDrawable(drawable)
                                        }
                                        Configuration.UI_MODE_NIGHT_NO -> {
                                            val drawable = ContextCompat.getDrawable(context, R.drawable.placeholder_image)
                                            imageView.setImageDrawable(drawable)
                                        }
                                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                            val drawable = ColorDrawable(Color.parseColor("#aaaaaa"))
                                            imageView.setImageDrawable(drawable)
                                        }
                                    }
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
                        ).apply(ConstantClass.REQUEST_OPTIONS_WITH_SIZE_100).into(imageView)
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
        if (viewType == TypeOfPost.ADD_NEW_POST.value) {
            val binding = FeedCardBinding.inflate(layoutInflater, parent, false)
            return this.AddNewItemViewHolder(binding)
        } else {
            val binding = FeedItemBinding.inflate(layoutInflater, parent, false)
            return this.FeedItemViewHolder(binding)
        }

    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is FeedItemViewHolder) {
            val customGridGroup = holder.itemView.findViewById<FrameLayout>(R.id.customGridGroup)
            val (_, videoIndex) = FeedController.peekVideoQueue()
            if (videoIndex != null && videoIndex < customGridGroup.childCount) {
                val child = customGridGroup[videoIndex]
                if (child is LoadingVideoView) {
                    child.pauseVideo()
                    FeedController.safeRemoveFromQueue()
                }
            }

        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is FeedItemViewHolder) {
            val customGridGroup = holder.itemView.findViewById<FrameLayout>(R.id.customGridGroup)
            for (i in 0 until customGridGroup.size) {
                val child = customGridGroup.getChildAt(i)
                if (child is LoadingVideoView) {
                    child.pauseVideo()
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

