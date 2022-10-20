package com.quangln2.customfeed.ui.screens.addpost

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.constants.ConstantClass
import com.quangln2.customfeed.data.database.FeedDatabase
import com.quangln2.customfeed.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.databinding.FragmentHomeScreenBinding
import com.quangln2.customfeed.others.utils.DownloadUtils
import com.quangln2.customfeed.others.utils.FileUtils
import com.quangln2.customfeed.ui.customview.CustomImageView
import com.quangln2.customfeed.ui.customview.CustomLayer
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.ui.customview.customgrid.getGridItemsLocation
import com.quangln2.customfeed.ui.viewmodel.FeedViewModel
import com.quangln2.customfeed.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
                    lifecycleScope.launch(Dispatchers.IO){
                        var flagHasCustomLayer = false
                        val count = data.clipData!!.itemCount
                        for (i in 0 until count) {
                            val uri = data.clipData!!.getItemAt(i).uri
                            val mimeType = context?.contentResolver?.getType(uri)
                            if (mimeType != null) {
                                if (mimeType.startsWith("image/")) {
                                    val imageView = CustomImageView.generateCustomImageView(requireContext(), uri.toString())
                                    listOfViews.add(imageView)
                                    listOfUris.add(uri)
                                } else if (mimeType.startsWith("video/")) {
                                    withContext(Dispatchers.Main){
                                        val videoView = LoadingVideoView(requireContext(), uri.toString())
                                        listOfViews.add(videoView)
                                        listOfUris.add(uri)
                                    }

                                }
                                if (listOfViews.size >= 10 && hasCustomLayer() == Pair(-1, -1) && !flagHasCustomLayer) {
                                    flagHasCustomLayer = true
                                    listOfViews.add(8, CustomLayer(requireContext()))
                                    listOfUris.add(8, Uri.EMPTY)
                                }
                            }

                            val uriForMultipart = data.clipData!!.getItemAt(i).uri
                            val ls = viewModel.uriLists.value
                            ls?.add(uriForMultipart)
                            viewModel._uriLists.postValue(ls?.toMutableList())
                        }
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
            initCustomGrid()

        }

        return binding.root
    }

    fun initCustomGrid(){
        val rectangles = getGridItemsLocation(listOfViews.size)
        val marginHorizontalSum = 16 + 32
        val widthGrid = Resources.getSystem().displayMetrics.widthPixels - marginHorizontalSum
        val contentPadding = 32

        for (i in listOfViews.indices) {
            when(val viewChild = listOfViews[i]){
                is CustomLayer -> {
                    viewChild.addedImagesText.text = "+${listOfViews.size - ConstantClass.MAXIMUM_IMAGE_IN_A_GRID}"
                    val layoutParams = ViewGroup.MarginLayoutParams(
                        (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding,
                        (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
                    ).apply {
                        leftMargin = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                        topMargin = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
                    }
                    viewChild.layoutParams = layoutParams
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
                    val layoutParams = ViewGroup.MarginLayoutParams(
                        (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding,
                        (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
                    ).apply {
                        leftMargin = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                        topMargin = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
                    }
                    viewChild.layoutParams = layoutParams
                    binding.customGridGroup.addView(viewChild)
                }
                is FrameLayout -> {
                    viewChild[1].setOnClickListener {
                        onHandleMoreImagesOrVideos(viewChild)
                    }
                    val layoutParams = ViewGroup.MarginLayoutParams(
                        (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding,
                        (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
                    ).apply {
                        leftMargin = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                        topMargin = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
                    }
                    viewChild.layoutParams = layoutParams
                    binding.customGridGroup.addView(viewChild)
                }
            }
        }
    }

    private fun retrieveFirstImageOrFirstVideo(uri: Uri, firstItemResolution: IntArray){
        val mimeType = DownloadUtils.getMimeType(uri.toString())
        if (mimeType != null && mimeType.startsWith("video")) {
            try {
                val bitmap = FileUtils.getVideoThumbnail(uri, requireContext())
                firstItemResolution[0] = bitmap.intrinsicWidth
                firstItemResolution[1] = bitmap.intrinsicHeight
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if(mimeType != null && mimeType.startsWith("image")){
            Glide.with(requireContext()).load(uri).into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    firstItemResolution[0] = resource.intrinsicWidth
                    firstItemResolution[1] = resource.intrinsicHeight
                }
            })
        }
    }

    fun loadInitialProfile(){
        // Load initial avatar
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(100)
        Glide.with(requireContext()).load(ConstantClass.AVATAR_LINK).apply(requestOptions).into(binding.myAvatarImage)
    }
    private fun onHandleMoreImagesOrVideos(customView: View) {
        val imageAboutDeleted = listOfViews.indexOf(customView)
        val (index, textValue) = hasCustomLayer()

        if (index == -1 && textValue == -1) {
            listOfViews.remove(customView)
            listOfUris.removeAt(imageAboutDeleted)
            binding.customGridGroup.removeView(customView)
        } else {
            if (textValue == 2) {
                binding.customGridGroup.removeViewAt(imageAboutDeleted)
                binding.customGridGroup.removeViewAt(index - 1)
                listOfViews.removeAt(imageAboutDeleted)
                listOfViews.removeAt(index - 1)

                listOfUris.removeAt(imageAboutDeleted)
                listOfUris.removeAt(index - 1)

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

                listOfUris.removeAt(imageAboutDeleted)
                listOfUris.removeAt(index - 1)
                val firstAddedItem = listOfViews[index - 1]
                binding.customGridGroup.addView(firstAddedItem)

                val view = CustomLayer(requireContext())
                view.addedImagesText.text = "+${textValue - 1}"
                binding.customGridGroup.addView(view)


                if (listOfViews.size >= 10) {
                    listOfViews.add(8, CustomLayer(requireContext()))
                    listOfUris.add(8, Uri.EMPTY)
                }
            }
        }
        binding.customGridGroup.removeAllViews()

        initCustomGrid()

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

    private fun preUploadFiles(): Pair <MutableList<Uri>, String> {
        val mutableLists = mutableListOf<Uri>()
        for (i in 0 until listOfUris.size) {
            if (listOfUris[i] != Uri.EMPTY) {
                mutableLists.add(listOfUris[i])
            }
        }
        val caption = binding.textField.editText?.text.toString()
        return Pair(mutableLists, caption)
    }
}