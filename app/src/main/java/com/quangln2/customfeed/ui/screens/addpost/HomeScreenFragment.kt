package com.quangln2.customfeed.ui.screens.addpost

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.constants.ConstantSetup
import com.quangln2.customfeed.data.database.FeedDatabase
import com.quangln2.customfeed.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.databinding.FragmentHomeScreenBinding
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

const val TAG = "HomeScreenFragment"
const val KEY_INSTANCE = "arrayListOfUris"
const val KEY_LISTS_OF_URL_RETURN = "listOfUrisReturn"
const val IMAGE_MIMETYPE_PART = "image/"
const val VIDEO_MIMETYPE_PART = "video/"
const val IMAGE_MIMETYPE = "image/*"
const val VIDEO_MIMETYPE = "video/*"

class HomeScreenFragment : Fragment() {
    private lateinit var binding: FragmentHomeScreenBinding

    private val database by lazy { FeedDatabase.getFeedDatabase(requireContext()) }
    private val viewModel: FeedViewModel by activityViewModels {
        ViewModelFactory(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    }
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                if (data.clipData != null) {
                    val uriSource = mutableListOf<Uri>()
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        uriSource.add(data.clipData!!.getItemAt(i).uri)
                    }
                    addAnImageOrVideoToList(uriSource)
                }
            }
        }
    }

    private var listOfViews: MutableList<View> = mutableListOf()
    private var listOfUris: MutableList<Uri> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel._uriLists.value?.clear()

        val arrayListOfUris = savedInstanceState?.getStringArrayList(KEY_INSTANCE) ?: arrayListOf()
        val uriSource = arrayListOfUris.map { uriString -> uriString.toUri() }.toMutableList()
        addAnImageOrVideoToList(uriSource)
        Log.v(TAG, "onSaveInstanceState listOfViews: ${listOfViews.size} and listOfUris: ${listOfUris.size}")
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        loadInitialProfile()
        buttonHandleChooseImagesOrVideos()
        buttonHandleSubmitToServer()

        viewModel.uriLists.observe(viewLifecycleOwner) {
            if (listOfViews.size != 0) {
                binding.customGridGroup.removeAllViews()
            }
            initCustomGrid()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<String>>(KEY_LISTS_OF_URL_RETURN)
            ?.observe(viewLifecycleOwner) {
                listOfUris.clear()
                listOfViews.clear()
                binding.customGridGroup.removeAllViews()
                val uriSource = it.map { uriString -> uriString.toUri() }.toMutableList()
                addAnImageOrVideoToList(uriSource)
                navController.currentBackStackEntry?.savedStateHandle?.remove<ArrayList<String>>(KEY_LISTS_OF_URL_RETURN)
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.v(TAG, "onSaveInstanceState listOfViews: ${listOfViews.size} and listOfUris: ${listOfUris.size}")

        val arrayOfString = arrayListOf<String>()
        for (uri in listOfUris) {
            if(uri != Uri.EMPTY) {
                arrayOfString.add(uri.toString())
            }
        }
        outState.putStringArrayList(KEY_INSTANCE, arrayOfString)

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel._uriLists.value?.clear()
    }

    private fun buttonHandleChooseImagesOrVideos(){
        // Handle choose image or video
        binding.buttonChooseImageVideo.setOnClickListener {
            val isStoragePermissionAllowed = FileUtils.getPermissionForStorageWithMultipleTimesDenial(requireContext())
            if (isStoragePermissionAllowed) {
                val pickerIntent = Intent(Intent.ACTION_PICK)
                pickerIntent.apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(IMAGE_MIMETYPE, VIDEO_MIMETYPE))
                }
                resultLauncher.launch(pickerIntent)
            }
        }
    }

    private fun loadInitialProfile() {
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).format(DecodeFormat.PREFER_RGB_565).override(100)
        Glide.with(requireContext()).load(ConstantSetup.AVATAR_LINK).apply(requestOptions).into(binding.myAvatarImage)
    }

    private fun buttonHandleSubmitToServer() {
        // Handle submit to server
        binding.buttonSubmitToServer.setOnClickListener {
            val (uriLists, caption) = preUploadFiles()
            if (caption.isEmpty() && uriLists.size == 0) {
                Toast.makeText(context, resources.getString(R.string.please_add_content), Toast.LENGTH_SHORT).show()
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

    private fun preUploadFiles(): Pair<MutableList<Uri>, String> {
        val mutableLists = mutableListOf<Uri>()
        for (i in 0 until listOfUris.size) {
            if (listOfUris[i] != Uri.EMPTY) {
                mutableLists.add(listOfUris[i])
            }
        }
        val caption = binding.textField.editText?.text.toString()
        return Pair(mutableLists, caption)
    }

    private fun addAnImageOrVideoToList(uriSource: List<Uri>){
        lifecycleScope.launch(Dispatchers.IO) {
            val ls = viewModel.uriLists.value
            for (uri in uriSource) {
                val mimeType = context?.contentResolver?.getType(uri)
                if (mimeType != null) {
                    if (mimeType.startsWith(IMAGE_MIMETYPE_PART)) {
                        withContext(Dispatchers.Main) {
                            val imageView = CustomImageView.generateCustomImageView(requireContext(), uri.toString())
                            listOfViews.add(imageView)
                            listOfUris.add(uri)
                        }
                    } else if (mimeType.startsWith(VIDEO_MIMETYPE_PART)) {
                        withContext(Dispatchers.Main) {
                            val videoView = LoadingVideoView(requireContext(), uri.toString())
                            listOfViews.add(videoView)
                            listOfUris.add(uri)
                        }
                    }
                }
                ls?.add(uri)
            }
            if (listOfViews.size >= 10 && hasCustomLayer() == Pair(-1, -1)) {
                listOfViews.add(8, CustomLayer(requireContext()))
                listOfUris.add(8, Uri.EMPTY)
            }
            viewModel._uriLists.postValue(ls?.toMutableList())
        }
    }

    private fun initCustomGrid() {
        val rectangles = getGridItemsLocation(listOfViews.size)
        val marginLeft = 8
        val contentPadding = 32
        val marginHorizontalSum = 2 * marginLeft + contentPadding
        val widthGrid = Resources.getSystem().displayMetrics.widthPixels - marginHorizontalSum

        for (i in rectangles.indices) {
            val leftView = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
            val topView = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
            val widthView = (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding
            val heightView = (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
            when (val viewChild = listOfViews[i]) {
                is CustomLayer -> {
                    val customLayerSize = listOfViews.filterIsInstance<CustomLayer>().size
                    viewChild.addedImagesText.text = "+${listOfViews.size - ConstantSetup.MAXIMUM_IMAGE_IN_A_GRID - customLayerSize}"
                    val layoutParams = ViewGroup.MarginLayoutParams(widthView,heightView).apply {
                        leftMargin = leftView
                        topMargin = topView
                    }
                    viewChild.layoutParams = layoutParams
                    viewChild.setOnClickListener {
                        //First: remove all views
                        binding.customGridGroup.removeAllViews()

                        //Second: pass Uri ArrayList to the next fragment
                        val listsOfNotEmptyUri = listOfUris.filter { it != Uri.EMPTY }
                        val arrayListsOfUri = ArrayList<String>()
                        listsOfNotEmptyUri.forEach {
                            arrayListsOfUri.add(it.toString())
                        }

                        findNavController().navigate(R.id.action_homeScreenFragment_to_viewDetailFragment,
                            Bundle().apply {
                                putStringArrayList("listOfUris", arrayListsOfUri)
                            },
                            navOptions {
                                anim {
                                    enter = android.R.animator.fade_in
                                    exit = android.R.animator.fade_out
                                }
                            })
                    }
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
                    val layoutParams = ViewGroup.MarginLayoutParams(widthView, heightView).apply {
                        leftMargin = leftView
                        topMargin = topView
                    }
                    viewChild.layoutParams = layoutParams
                    binding.customGridGroup.addView(viewChild)
                }
                is FrameLayout -> {
                    viewChild[1].setOnClickListener {
                        onHandleMoreImagesOrVideos(viewChild)
                    }
                    val layoutParams = ViewGroup.MarginLayoutParams(widthView, heightView).apply {
                        leftMargin = leftView
                        topMargin = topView
                    }
                    viewChild.layoutParams = layoutParams
                    binding.customGridGroup.addView(viewChild)
                }
            }
        }
    }
}