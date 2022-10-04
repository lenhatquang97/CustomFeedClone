package com.quangln2.customfeed.ui.screens.viewimageorvideo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.quangln2.customfeed.R
import com.quangln2.customfeed.databinding.FragmentViewMoreBinding
import com.quangln2.customfeed.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeed.data.models.UploadPost
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.others.utils.FileUtils
import com.quangln2.customfeed.ui.viewmodel.FeedViewModel
import com.quangln2.customfeed.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewMoreFragment : Fragment() {
    private lateinit var binding: FragmentViewMoreBinding
    private val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(), RemoteDataSourceImpl()))
    }
    private lateinit var item: UploadPost
    private val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(100)

    private fun fetchPostById(id: String) {
        item = viewModel.getFeedItem(id)
        binding.myName.text = item.name
        binding.createdTime.text = FileUtils.convertUnixTimestampToTime(item.createdTime)
        binding.caption.text = item.caption

        Glide.with(requireContext()).load(item.avatar).apply(requestOptions).into(binding.myAvatarImage)

        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0 until item.imagesAndVideos.size) {
                val value = item.imagesAndVideos[i]
                if (value.contains("mp4")) {
                    withContext(Dispatchers.Main) {
                        val imageView = ImageView(context)
                        Glide.with(requireContext()).load(value).thumbnail(0.1f).apply(requestOptions).into(imageView)
                        imageView.setOnClickListener {
                            findNavController().navigate(
                                R.id.action_allFeedsFragment_to_viewFullVideoFragment,
                                Bundle().apply { putString("url", value) })
                        }
                        binding.extendedCustomGridGroup.addView(imageView)
                    }
                } else {
                    val imageView = ImageView(context)
                    imageView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                    imageView.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_viewMoreFragment_to_viewFullVideoFragment,
                            Bundle().apply { putString("url", value) })
                    }
                    Glide.with(requireContext()).load(value).apply(requestOptions)
                        .into(object : SimpleTarget<Drawable>() {
                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                CoroutineScope(Dispatchers.Main).launch {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments?.getString("id")
        if (id != null && id.isNotEmpty()) fetchPostById(id)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewMoreBinding.inflate(inflater, container, false)
        return binding.root
    }
}