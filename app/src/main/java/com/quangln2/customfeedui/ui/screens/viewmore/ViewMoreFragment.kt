package com.quangln2.customfeedui.ui.screens.viewmore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.database.FeedDatabase
import com.quangln2.customfeedui.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.databinding.FragmentViewMoreBinding
import com.quangln2.customfeedui.others.utils.FileUtils
import com.quangln2.customfeedui.ui.viewmodel.ViewMoreViewModel
import com.quangln2.customfeedui.ui.viewmodelfactory.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewMoreFragment : Fragment() {
    private lateinit var binding: FragmentViewMoreBinding

    private val database by lazy {
        FeedDatabase.getFeedDatabase(requireContext())
    }
    private val viewModel: ViewMoreViewModel by viewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }
    private val navTransition by lazy {
        navOptions {
            anim {
                enter = android.R.animator.fade_in
                exit = android.R.animator.fade_out
            }
        }
    }

    private fun initializeHeaderAndCaption(item: MyPostRender){
        binding.myName.text = item.name
        binding.createdTime.text = FileUtils.convertUnixTimestampToTime(item.createdTime)
        binding.caption.text = item.caption
        Glide.with(requireContext()).load(item.avatar).apply(ConstantSetup.REQUEST_OPTIONS_WITH_SIZE_100).into(binding.myAvatarImage)
    }

    private fun onViewDetailImageOrVideo(item: MyPostRender, index: Int){
        val urlArrayList = ArrayList<String>()
        item.resources.forEach {
            urlArrayList.add(it.url)
        }
        findNavController().navigate(
            R.id.action_viewMoreFragment_to_viewFullVideoFragment,
            Bundle().apply {
                putString("value", item.resources[index].url)
                putStringArrayList("listOfUrls", urlArrayList)
            }, navTransition
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments?.getString("id")
        if (id != null && id.isNotEmpty()){
            viewModel.getPostWithId(id)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewMoreBinding.inflate(inflater, container, false)
        viewModel.postItem.observe(viewLifecycleOwner){
            it?.apply {
                val item = MyPostRender.convertMyPostToMyPostRender(this)
                initializeHeaderAndCaption(item)
                lifecycleScope.launch(Dispatchers.IO) {
                    for (i in 0 until item.resources.size) {
                        val (value, mimeType) = viewModel.retrieveValueAndMimeType(item, i, requireContext())
                        mimeType?.apply {
                            withContext(Dispatchers.Main){
                                val imageView = ImageView(context)
                                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                                imageView.setOnClickListener {
                                    onViewDetailImageOrVideo(item, i)
                                }
                                Glide.with(requireContext()).load(value)
                                    .apply(ConstantSetup.REQUEST_OPTIONS_WITH_SIZE_100)
                                    .into(imageView)
                                binding.extendedCustomGridGroup.addView(imageView)
                            }
                        }
                    }
                }
            }
        }
        return binding.root
    }
}