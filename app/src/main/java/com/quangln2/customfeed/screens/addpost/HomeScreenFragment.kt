package com.quangln2.customfeed.screens.addpost

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.quangln2.customfeed.R
import com.quangln2.customfeed.constants.ConstantClass
import com.quangln2.customfeed.customview.CustomLayer
import com.quangln2.customfeed.databinding.FragmentHomeScreenBinding
import com.quangln2.customfeed.utils.FileUtils
import com.quangln2.customfeed.viewmodel.FeedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*


class HomeScreenFragment : Fragment() {

    private lateinit var binding: FragmentHomeScreenBinding
    private val viewModel: FeedViewModel by activityViewModels()
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
                                println(mimeType)
                                if (mimeType.startsWith("image/")) {
                                    val imageView = ImageView(context)
                                    imageView.layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                                    imageView.setImageURI(uri)
                                    listOfViews.add(imageView)
                                    if (listOfViews.size >= 10) {
                                        listOfViews[8] = CustomLayer(requireContext())
                                    }

                                } else if (mimeType.startsWith("video/")) {
                                    val videoView = VideoView(requireContext())
                                    videoView.layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    videoView.setVideoURI(uri)
                                    videoView.setBackgroundDrawable(FileUtils.getVideoThumbnail(uri, requireContext()))

                                    videoView.setOnClickListener {
                                        videoView.setBackgroundDrawable(null)
                                        videoView.seekTo(0)
                                        videoView.start()
                                    }
                                    withContext(Dispatchers.Main) {
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


    private fun uploadFiles() {
        if (viewModel.uriLists.value?.isEmpty()!!) {
            return
        }
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("feedId", UUID.randomUUID().toString())
        builder.addFormDataPart("name", ConstantClass.NAME)
        builder.addFormDataPart("avatar", ConstantClass.AVATAR_LINK)
        builder.addFormDataPart("createdTime", System.currentTimeMillis().toString())
        builder.addFormDataPart("caption", binding.textField.editText?.text.toString())

        for (uriItr in viewModel.uriLists.value!!) {
            val tmp = FileUtils.getRealPathFromURI(uriItr, requireContext())
            if (tmp != null) {
                val file = File(tmp)
                val requestFile = RequestBody.create(
                    if (uriItr.toString()
                            .contains("mp4")
                    ) "video/*".toMediaTypeOrNull() else "image/*".toMediaTypeOrNull(), file
                )
                builder.addFormDataPart("upload", file.name, requestFile)
            }
        }
        try {
            viewModel.uploadFiles(builder.build().parts, requireContext())
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel._uriLists.value?.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeScreenBinding.inflate(inflater, container, false)

        Glide.with(requireContext()).load(ConstantClass.AVATAR_LINK).into(binding.myAvatarImage)


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
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        return binding.root
    }
}