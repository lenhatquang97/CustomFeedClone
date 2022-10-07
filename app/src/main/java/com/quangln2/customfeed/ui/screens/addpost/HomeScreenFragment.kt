package com.quangln2.customfeed.ui.screens.addpost

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.quangln2.customfeed.ui.customview.CustomLayer
import com.quangln2.customfeed.ui.customview.VideoThumbnailView
import com.quangln2.customfeed.ui.viewmodel.FeedViewModel
import com.quangln2.customfeed.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*


class HomeScreenFragment : Fragment() {

    private lateinit var binding: FragmentHomeScreenBinding

    private val database by lazy {
        FeedDatabase.getFeedDatabase(requireContext())
    }
    private val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }
    private var listOfViews: MutableList<View> = mutableListOf()

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                if (data.clipData != null) {
                    val count = data.clipData!!.itemCount
                    lifecycleScope.launch(Dispatchers.IO) {
                        for (i in 0 until count) {
                            val uri = data.clipData!!.getItemAt(i).uri
                            val mimeType = context?.contentResolver?.getType(uri)
                            if (mimeType != null) {
                                if (mimeType.startsWith("image/")) {
                                    val imageView = ImageView(context)
                                    imageView.apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        scaleType = ImageView.ScaleType.CENTER_CROP
                                        setImageURI(uri)
                                    }
                                    listOfViews.add(imageView)
                                    if (listOfViews.size >= 10) listOfViews[8] = CustomLayer(requireContext())
                                } else if (mimeType.startsWith("video/")) {
                                    withContext(Dispatchers.Main) {
                                        val videoView = VideoThumbnailView(requireContext(), uri.toString())
                                        videoView.apply {
                                            layoutParams = ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            playButton.visibility = View.VISIBLE
                                        }

                                        listOfViews.add(videoView)
                                        if (listOfViews.size >= 10) {
                                            listOfViews[8] = CustomLayer(requireContext())
                                        }
                                    }
                                }
                            }

                            val clipItem = data.clipData!!.getItemAt(i)
                            val uriForMultipart = clipItem.uri
                            val ls = viewModel._uriLists.value
                            ls?.add(uriForMultipart)
                            viewModel._uriLists.postValue(ls?.toMutableList())
                        }

                    }
                } else if (data.data != null) {
                    val imgPath = data.data!!.path
                    println("Image path is: $imgPath")
                }
            }
        }
    }

    private fun compressImagesAndVideos(uriLists: MutableList<Uri>): LiveData<MutableList<Uri>> {
        val result: MutableList<Uri> = mutableListOf()
        for (uri in uriLists) {
            val mimeTypeForMultipart = context?.contentResolver?.getType(uri)
            val file = File(requireContext().filesDir, "${UUID.randomUUID().toString()}.jpg")
            if (mimeTypeForMultipart != null) {
                if (mimeTypeForMultipart.startsWith("image/")) {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)
                    result.add(file.toUri())
                } else if (mimeTypeForMultipart.startsWith("video/")) {
                    result.add(uri)
                }
            }
        }

        return MutableLiveData<MutableList<Uri>>().apply { value = result }
    }


    private fun uploadFiles() {
        if (viewModel.uriLists.value!= null && viewModel.uriLists.value?.isNotEmpty()!!){
            val uriListsForCompressing = compressImagesAndVideos(viewModel.uriLists.value!!)
            if (viewModel.uriLists.value?.isEmpty()!!) {
                Toast.makeText(requireContext(), "No file to upload", Toast.LENGTH_SHORT).show()
                return
            }

            val parts = viewModel.uploadMultipartBuilder(
                binding.textField.editText?.text.toString(),
                uriListsForCompressing,
                requireContext()
            )
            try {
                viewModel.uploadFiles(parts, requireContext())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val parts = viewModel.uploadMultipartBuilder(
                binding.textField.editText?.text.toString(),
                MutableLiveData<MutableList<Uri>>().apply { value = mutableListOf() },
                requireContext()
            )
            try {
                viewModel.uploadFiles(parts, requireContext())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }








    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel._uriLists.value?.clear()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            uploadFiles()
        }
        binding.buttonGetAllPosts.setOnClickListener {
            listOfViews.clear()
            findNavController().navigate(R.id.action_homeScreenFragment_to_allFeedsFragment)

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
                    binding.customGridGroup.addView(viewChild)
                }
            }
        }

        viewModel.isUploading.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        return binding.root
    }
}