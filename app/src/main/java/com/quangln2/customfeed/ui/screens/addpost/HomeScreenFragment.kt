package com.quangln2.customfeed.ui.screens.addpost

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.constants.ConstantClass
import com.quangln2.customfeed.data.database.FeedDatabase
import com.quangln2.customfeed.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.databinding.FragmentHomeScreenBinding
import com.quangln2.customfeed.others.utils.FileUtils
import com.quangln2.customfeed.ui.customview.CustomImageView
import com.quangln2.customfeed.ui.customview.CustomLayer
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.ui.viewmodel.FeedViewModel
import com.quangln2.customfeed.ui.viewmodel.ViewModelFactory


class HomeScreenFragment : Fragment() {

    lateinit var binding: FragmentHomeScreenBinding

    private val database by lazy {
        FeedDatabase.getFeedDatabase(requireContext())
    }
    private val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }
    var listOfViews: MutableList<View> = mutableListOf()
    var listOfUris: MutableList<Uri> = mutableListOf()


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
                                //getFirstImageWidthAndHeight(i, uri)
                                val imageView = CustomImageView.generateCustomImageView(requireContext(), uri.toString())
                                listOfViews.add(imageView)
                                listOfUris.add(uri)
                            } else if (mimeType.startsWith("video/")) {
                                //getFirstVideoWidthAndHeight(i, uri)
                                val videoView = LoadingVideoView(requireContext(), uri.toString())
                                listOfViews.add(videoView)
                                listOfUris.add(uri)
                            }
                            if (listOfViews.size >= 10 && hasCustomLayer() == Pair(-1, -1)) {
                                listOfViews.add(8, CustomLayer(requireContext()))
                                listOfUris.add(8, Uri.EMPTY)
                            }
                        }

                        val uriForMultipart = data.clipData!!.getItemAt(i).uri
                        val ls = viewModel.uriLists.value
                        ls?.add(uriForMultipart)
                        viewModel._uriLists.value = ls?.toMutableList()
                    }
                }
            }
        }
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

        loadInitialProfile()

        // Handle choose image or video
        binding.buttonChooseImageVideo.setOnClickListener {
            val isStoragePermissionAllowed = FileUtils.getPermissionForStorageWithMultipleTimesDenial(requireContext(), this.requireActivity())
            if (isStoragePermissionAllowed) {
                val pickerIntent = Intent(Intent.ACTION_PICK)
                pickerIntent.type = "*/*"
                pickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                pickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                resultLauncher.launch(pickerIntent)
            }
        }

        // Handle submit to server
        binding.buttonSubmitToServer.setOnClickListener {
            val (uriLists, caption) = preUploadFiles()
            if (caption.isEmpty() && uriLists.size == 0) {
                Toast.makeText(context, ConstantClass.PLEASE_ADD_CONTENT, Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.action_homeScreenFragment_to_allFeedsFragment, null, navOptions {
                    anim {
                        enter = android.R.animator.fade_in
                        exit = android.R.animator.fade_out
                    }
                })
                viewModel.uploadFiles(caption, uriLists, requireContext())
            }
        }

        viewModel.uriLists.observe(viewLifecycleOwner) {
            if (listOfViews.size != 0) {
                binding.customGridGroup.removeAllViews()
            }
            for (i in listOfViews.indices) {
                when(val viewChild = listOfViews[i]){
                    is CustomLayer -> {
                        viewChild.addedImagesText.text = "+${listOfViews.size - ConstantClass.MAXIMUM_IMAGE_IN_A_GRID}"
                        binding.customGridGroup.addView(viewChild)
                        break
                    }
                    is LoadingVideoView -> {
                        viewChild.apply {
                            playButton.visibility = View.VISIBLE
                            crossButton.visibility = View.VISIBLE
                            crossButton.setOnClickListener {
                                onHandleMoreImagesOrVideos(viewChild)
                            }
                        }
                        binding.customGridGroup.addView(viewChild)
                    }
                    is FrameLayout -> {
                        viewChild[1].setOnClickListener {
                            onHandleMoreImagesOrVideos(viewChild)
                        }
                        binding.customGridGroup.addView(viewChild)
                    }
                }
            }
        }

        return binding.root
    }
}