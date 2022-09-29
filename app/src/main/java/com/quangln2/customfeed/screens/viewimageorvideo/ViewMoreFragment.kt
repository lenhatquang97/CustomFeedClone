package com.quangln2.customfeed.screens.viewimageorvideo

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.size
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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
import com.quangln2.customfeed.customview.CustomLayer
import com.quangln2.customfeed.customview.LoadingVideoView
import com.quangln2.customfeed.databinding.FragmentViewMoreBinding
import com.quangln2.customfeed.models.UploadPost
import com.quangln2.customfeed.utils.FileUtils
import com.quangln2.customfeed.viewmodel.FeedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewMoreFragment : Fragment() {
    private lateinit var binding: FragmentViewMoreBinding
    private val viewModel: FeedViewModel by activityViewModels()
    private lateinit var item: UploadPost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun fetchPostById(id: String){
        item = viewModel.getFeedItem(id)
        binding.myName.text = item.name
        binding.createdTime.text = FileUtils.convertUnixTimestampToTime(item.createdTime)
        binding.caption.text = item.caption
        Glide.with(requireContext()).load(item.avatar).into(binding.myAvatarImage)

        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0 until item.imagesAndVideos.size) {
                val value = item.imagesAndVideos[i]
                if (value.contains("mp4")) {
                    withContext(Dispatchers.Main) {
                        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(100)
                        val imageView = ImageView(context)
                        Glide.with(requireContext()).load(value).thumbnail(0.1f).apply(requestOptions)
                            .into(imageView)
                        imageView.setOnClickListener {
                            val bundle = Bundle()
                            bundle.putString("url", value)
                            findNavController().navigate(R.id.action_allFeedsFragment_to_viewFullVideoFragment, bundle)
                        }
                        binding.extendedCustomGridGroup.addView(imageView)
                    }
                } else {
                    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(100)
                    val imageView = ImageView(context)
                    imageView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                    imageView.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putString("url", value)
                        findNavController().navigate(R.id.action_viewMoreFragment_to_viewFullVideoFragment, bundle)
                    }
                    Glide.with(requireContext()).load(value).apply(requestOptions).into(object : SimpleTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            CoroutineScope(Dispatchers.Main).launch{
                                binding.extendedCustomGridGroup.firstWidth = resource.intrinsicWidth
                                binding.extendedCustomGridGroup.firstHeight = resource.intrinsicHeight
                            }
                            imageView.setImageDrawable(resource)
                        }

                    })
                    binding.extendedCustomGridGroup.addView(imageView)
                }
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewMoreBinding.inflate(inflater, container, false)
        val id = arguments?.getString("id")
        if(id != null && id.isNotEmpty()){
            fetchPostById(id)
        }
        return binding.root
    }
}