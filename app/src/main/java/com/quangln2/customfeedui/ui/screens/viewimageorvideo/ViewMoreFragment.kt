package com.quangln2.customfeedui.ui.screens.viewimageorvideo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.database.FeedDatabase
import com.quangln2.customfeedui.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.databinding.FragmentViewMoreBinding
import com.quangln2.customfeedui.others.utils.DownloadUtils
import com.quangln2.customfeedui.others.utils.FileUtils
import com.quangln2.customfeedui.ui.viewmodel.FeedViewModel
import com.quangln2.customfeedui.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewMoreFragment : Fragment() {
    private lateinit var binding: FragmentViewMoreBinding

    private val database by lazy {
        FeedDatabase.getFeedDatabase(requireContext())
    }
    private val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }
    private lateinit var item: MyPostRender


    private fun fetchPostById(id: String) {
        item = viewModel.getFeedItem(id)
        binding.myName.text = item.name
        binding.createdTime.text = FileUtils.convertUnixTimestampToTime(item.createdTime)
        binding.caption.text = item.caption

        Glide.with(requireContext()).load(item.avatar).apply(ConstantSetup.REQUEST_OPTIONS_WITH_SIZE_100).into(binding.myAvatarImage)

        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0 until item.resources.size) {
                val existsLocal = DownloadUtils.doesLocalFileExist(
                    item.resources[i].url,
                    requireContext()
                ) && DownloadUtils.isValidFile(item.resources[i].url, requireContext(), item.resources[i].size)
                val value = if (existsLocal) DownloadUtils.getTemporaryFilePath(
                    item.resources[i].url,
                    requireContext()
                ) else item.resources[i].url
                if (value.contains("mp4")) {
                    withContext(Dispatchers.Main) {
                        val imageView = ImageView(context)
                        Glide.with(requireContext()).load(value).thumbnail(0.1f).apply(ConstantSetup.REQUEST_OPTIONS_WITH_SIZE_100).into(imageView)
                        imageView.setOnClickListener {
                            val urlArrayList = ArrayList<String>()
                            item.resources.forEach {
                                urlArrayList.add(it.url)
                            }
                            findNavController().navigate(
                                R.id.action_viewMoreFragment_to_viewFullVideoFragment,
                                Bundle().apply {
                                    putString("value", item.resources[i].url)
                                    putStringArrayList(
                                        "listOfUrls",
                                        urlArrayList
                                    )
                                },
                                navOptions {
                                    anim {
                                        enter = android.R.animator.fade_in
                                        exit = android.R.animator.fade_out
                                    }
                                }
                            )
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
                        val urlArrayList = ArrayList<String>()
                        item.resources.forEach {
                            urlArrayList.add(it.url)
                        }
                        findNavController().navigate(
                            R.id.action_viewMoreFragment_to_viewFullVideoFragment,
                            Bundle().apply {
                                putString("value", item.resources[i].url)
                                putStringArrayList(
                                    "listOfUrls",
                                    urlArrayList
                                )
                            },
                            navOptions {
                                anim {
                                    enter = android.R.animator.fade_in
                                    exit = android.R.animator.fade_out
                                }
                            }
                        )
                    }
                    Glide.with(requireContext()).load(value).apply(ConstantSetup.REQUEST_OPTIONS_WITH_SIZE_100)
                        .into(object : SimpleTarget<Drawable>() {
                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    binding.extendedCustomGridGroup.firstItemWidth = resource.intrinsicWidth
                                    binding.extendedCustomGridGroup.firstItemHeight = resource.intrinsicHeight
                                }
                                imageView.setImageDrawable(resource)
                            }
                        })
                    withContext(Dispatchers.Main) {
                        binding.extendedCustomGridGroup.addView(imageView)
                    }
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
        if (id != null && id.isNotEmpty()) fetchPostById(id)
        return binding.root
    }
}