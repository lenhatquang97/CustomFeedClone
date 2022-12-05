package com.quangln2.customfeedui.ui.screens.addpost

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.databinding.FragmentViewDetailBinding
import com.quangln2.customfeedui.ui.customview.CustomImageView
import com.quangln2.customfeedui.ui.customview.LoadingVideoView
import com.quangln2.customfeedui.ui.customview.customgrid.getGridItemsLocationWithMoreThanTen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewDetailFragment : Fragment() {
    private lateinit var binding: FragmentViewDetailBinding
    private var listOfViews: MutableList<View> = mutableListOf()
    private var listOfUris: MutableList<String> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arrayListOfUris = arguments?.getStringArrayList("listOfUris")
        arrayListOfUris?.forEach {
            listOfUris.add(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewDetailBinding.inflate(inflater, container, false)

        binding.toolbar.title = resources.getString(R.string.nums_of_images_videos, listOfViews.size)

        binding.doneTitle.setOnClickListener {
            val navController = findNavController()
            val arrayListOfReturn = arrayListOf<String>()
            listOfUris.forEach {
                arrayListOfReturn.add(it)
            }
            navController.previousBackStackEntry?.savedStateHandle?.set("listOfUrisReturn", arrayListOfReturn)
            navController.popBackStack()
        }

        binding.toolbar.setNavigationOnClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle(resources.getString(R.string.not_saved_changes))
                setMessage(resources.getString(R.string.cancel_ask))
                setPositiveButton(requireContext().getText(R.string.positive_title)) { _, _ ->
                    findNavController().popBackStack()
                }
                setNegativeButton(requireContext().getText(R.string.negative_title)) { dialog, _ ->
                    dialog.dismiss()
                }
            }.show()
        }
        initCustomGrid(listOfUris)
        return binding.root
    }

    private fun initCustomGrid(listOfUris: MutableList<String>) {
        val rectangles = getGridItemsLocationWithMoreThanTen(listOfUris.size)
        val marginHorizontalSum = 16 + 32
        val widthGrid = Resources.getSystem().displayMetrics.widthPixels - marginHorizontalSum
        val contentPadding = 16
        lifecycleScope.launch(Dispatchers.IO) {
            //This will prevent ConcurrentModificationException
            val iterator = listOfUris.toMutableList().iterator()
            var i = 0
            while (iterator.hasNext()) {
                val it = iterator.next()
                val mimeType = requireContext().contentResolver.getType(it.toUri())
                if (mimeType != null) {
                    val layoutParams = ViewGroup.MarginLayoutParams(
                        (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding,
                        (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
                    ).apply {
                        leftMargin = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                        topMargin = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
                    }
                    if (mimeType.contains("image")) {
                        withContext(Dispatchers.Main) {
                            val imageView = CustomImageView.generateCustomImageView(requireContext(), it)
                            imageView[1].setOnClickListener {
                                onHandleMoreImagesOrVideos(imageView, listOfUris)
                            }
                            imageView.layoutParams = layoutParams
                            listOfViews.add(imageView)
                            binding.customGridGroup.addView(imageView)
                        }
                    } else if (mimeType.contains("video")) {
                        withContext(Dispatchers.Main) {
                            //TODO: Get thumbnail of video
                            val videoView = LoadingVideoView(requireContext(), it, "")
                            videoView.apply {
                                progressBar.visibility = View.GONE
                                crossButton.visibility = View.VISIBLE
                                crossButton.setOnClickListener {
                                    onHandleMoreImagesOrVideos(videoView, listOfUris)
                                }
                            }
                            videoView.layoutParams = layoutParams
                            listOfViews.add(videoView)
                            binding.customGridGroup.addView(videoView)
                        }
                    }
                }
                i++
            }
            withContext(Dispatchers.Main) {
                binding.toolbar.title = resources.getString(R.string.nums_of_images_videos, listOfViews.size)
            }

        }
    }

    private fun onHandleMoreImagesOrVideos(customView: View, listOfUris: MutableList<String>) {
        val indexDeleted = listOfViews.indexOf(customView)
        listOfViews.removeAt(indexDeleted)
        listOfUris.removeAt(indexDeleted)

        binding.customGridGroup.removeAllViews()
        listOfViews.clear()
        initCustomGrid(listOfUris)

    }

}