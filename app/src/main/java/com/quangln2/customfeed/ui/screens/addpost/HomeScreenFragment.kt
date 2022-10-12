package com.quangln2.customfeed.ui.screens.addpost

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.constants.ConstantClass
import com.quangln2.customfeed.data.database.FeedDatabase
import com.quangln2.customfeed.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.databinding.FragmentHomeScreenBinding
import com.quangln2.customfeed.ui.customview.CustomImageView
import com.quangln2.customfeed.ui.customview.CustomLayer
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.ui.viewmodel.FeedViewModel
import com.quangln2.customfeed.ui.viewmodel.ViewModelFactory


class HomeScreenFragment : Fragment() {

    private lateinit var binding: FragmentHomeScreenBinding

    private val database by lazy {
        FeedDatabase.getFeedDatabase(requireContext())
    }
    private val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }
    private var listOfViews: MutableList<View> = mutableListOf()

    private fun onHandleMoreImagesOrVideos(customView: View){
        val imageAboutDeleted = listOfViews.indexOf(customView)
        val (index, textValue) = hasCustomLayer()
        println("index: $index, textValue: $textValue, imageAboutDeleted: $imageAboutDeleted")
        if(index == -1 && textValue == -1) {
            listOfViews.remove(customView)
            binding.customGridGroup.removeView(customView)
        } else {
            if(textValue == 2) {
                binding.customGridGroup.removeViewAt(imageAboutDeleted)
                binding.customGridGroup.removeViewAt(index - 1)
                listOfViews.removeAt(imageAboutDeleted)
                listOfViews.removeAt(index - 1)
                val start = index - 1
                for (i in start until listOfViews.size) {
                    val view = listOfViews[i]
                    binding.customGridGroup.addView(view)
                }


            } else {
                binding.customGridGroup.removeViewAt(imageAboutDeleted)
                binding.customGridGroup.removeViewAt(index - 1)
                listOfViews.removeAt(imageAboutDeleted)
                listOfViews.removeAt(index - 1)
                println("start: ${index-1} ${listOfViews.size}")

                binding.customGridGroup.addView(listOfViews[index - 1])


                val view = CustomLayer(requireContext())
                view.addedImagesText.text = "+${textValue - 1}"
                binding.customGridGroup.addView(view)
                if (listOfViews.size >= 10) listOfViews.add(8, CustomLayer(requireContext()))



            }
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                if (data.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val uri = data.clipData!!.getItemAt(i).uri
                        val mimeType = context?.contentResolver?.getType(uri)
                        if (mimeType != null) {
                            if (mimeType.startsWith("image/")) {
                                val imageView = CustomImageView(requireContext(), uri.toString())
                                imageView.apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    crossButton.setOnClickListener {
                                        onHandleMoreImagesOrVideos(imageView)
                                    }
                                }

                                listOfViews.add(imageView)
                            } else if (mimeType.startsWith("video/")) {
                                val videoView = LoadingVideoView(requireContext(), uri.toString())
                                videoView.apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    playButton.visibility = View.VISIBLE
                                    crossButton.visibility = View.VISIBLE
                                    crossButton.setOnClickListener {
                                        onHandleMoreImagesOrVideos(videoView)
                                    }
                                }

                                listOfViews.add(videoView)
                            }
                            if (listOfViews.size >= 10 && hasCustomLayer() == Pair(-1,-1)) listOfViews.add(8, CustomLayer(requireContext()))
                        }

                        val clipItem = data.clipData!!.getItemAt(i)
                        val uriForMultipart = clipItem.uri
                        val ls = viewModel.uriLists.value
                        ls?.add(uriForMultipart)
                        viewModel._uriLists.value = ls?.toMutableList()
                    }
                } else if (data.data != null) {
                    val imgPath = data.data!!.path
                    println("Image path is: $imgPath")
                }
            }
        }
    }

    private fun hasCustomLayer(): Pair<Int, Int> {
        for (i in 8 until binding.customGridGroup.childCount) {
            val view = binding.customGridGroup.getChildAt(i)
            if (view is CustomLayer) {
                val text = view.addedImagesText.text.toString()
                val value = text.substring(1, text.length).toInt()
                return Pair(i, value)
            }
        }
        return Pair(-1, -1)
    }


    private fun uploadFiles() {
        val mutableLists = mutableListOf<Uri>()
        for(i in 0 until binding.customGridGroup.childCount) {
            val view = binding.customGridGroup.getChildAt(i)
            if(view is CustomImageView) {
                mutableLists.add(view.url.toUri())
            } else if(view is LoadingVideoView) {
                mutableLists.add(view.url.toUri())
            }
        }
        val caption = binding.textField.editText?.text.toString()
        viewModel.uploadFiles(caption, mutableLists, requireContext())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel._uriLists.value?.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel._uriLists.value?.clear()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeScreenBinding.inflate(inflater, container, false)

        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(100)
        Glide.with(requireContext()).load(ConstantClass.AVATAR_LINK).apply(requestOptions).into(binding.myAvatarImage)


        binding.buttonChooseImageVideo.setOnClickListener {
            val pickerIntent = Intent(Intent.ACTION_PICK)
            pickerIntent.type = "*/*"
            pickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            pickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            resultLauncher.launch(pickerIntent)
        }
        binding.buttonSubmitToServer.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_allFeedsFragment, null, navOptions {
                anim {
                    enter = android.R.animator.fade_in
                    exit = android.R.animator.fade_out
                }
            })
            uploadFiles()
        }


        viewModel.uriLists.observe(viewLifecycleOwner) {
            if (listOfViews.size != 0) {
                binding.customGridGroup.removeAllViews()
            }
            for (viewChild in listOfViews) {
                if (viewChild is CustomLayer) {
                    viewChild.addedImagesText.text = "+${listOfViews.size - 9}"
                    binding.customGridGroup.addView(viewChild)
                    break
                } else {
                    println("View is: $viewChild")
                    binding.customGridGroup.addView(viewChild)
                }
            }
        }

        return binding.root
    }
}