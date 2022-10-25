package com.quangln2.customfeed.ui.screens.addpost

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
import com.quangln2.customfeed.databinding.FragmentViewDetailBinding
import com.quangln2.customfeed.ui.customview.CustomImageView
import com.quangln2.customfeed.ui.customview.LoadingVideoView
import com.quangln2.customfeed.ui.customview.customgrid.getGridItemsLocationWithMoreThanTen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewDetailFragment : Fragment() {
    private lateinit var binding: FragmentViewDetailBinding
    var listOfViews: MutableList<View> = mutableListOf()
    var listOfUris: MutableList<String> = arrayListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewDetailBinding.inflate(inflater, container, false)
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
                setTitle("Some changes have not been saved")
                setMessage("Do you want to cancel them?")
                setPositiveButton("Yes") { _, _ ->
                    findNavController().popBackStack()
                }
                setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            }.show()
        }
        val arrayListOfUris = arguments?.getStringArrayList("listOfUris")
        arrayListOfUris?.forEach {
            listOfUris.add(it)
        }
        initCustomGrid(listOfUris)

        return binding.root
    }
    private fun initCustomGrid(listOfUris: MutableList<String>){
        val rectangles = getGridItemsLocationWithMoreThanTen(listOfUris.size)
        val marginHorizontalSum = 16 + 32
        val widthGrid = Resources.getSystem().displayMetrics.widthPixels - marginHorizontalSum
        val contentPadding = 16
        lifecycleScope.launch(Dispatchers.IO) {
            val iterator = listOfUris.toMutableList().iterator()
            var i = 0
            while(iterator.hasNext()){
                val it = iterator.next()
                val mimeType = requireContext().contentResolver.getType(it.toUri())
                if(mimeType != null){
                    if(mimeType.contains("image")){
                        withContext(Dispatchers.Main){
                            val imageView = CustomImageView.generateCustomImageView(requireContext(), it)
                            val layoutParams = ViewGroup.MarginLayoutParams(
                                (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding,
                                (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
                            ).apply {
                                leftMargin = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                                topMargin = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
                            }
                            imageView.layoutParams = layoutParams
                            imageView[1].setOnClickListener {
                                onHandleMoreImagesOrVideos(imageView, listOfUris)

                            }
                            listOfViews.add(imageView)
                            binding.customGridGroup.addView(imageView)
                        }
                    }else if(mimeType.contains("video")){
                        withContext(Dispatchers.Main){
                            val videoView = LoadingVideoView(requireContext(), it)
                            videoView.apply {
                                progressBar.visibility = View.GONE
                                crossButton.visibility = View.VISIBLE
                                crossButton.setOnClickListener {
                                    onHandleMoreImagesOrVideos(videoView, listOfUris)
                                }
                            }

                            val layoutParams = ViewGroup.MarginLayoutParams(
                                (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding,
                                (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
                            ).apply {
                                leftMargin = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
                                topMargin = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
                            }
                            videoView.layoutParams = layoutParams
                            listOfViews.add(videoView)
                            binding.customGridGroup.addView(videoView)
                        }
                    }
                }
                i++
            }
        }
    }

    private fun onHandleMoreImagesOrVideos(customView: View, listOfUris: MutableList<String>){
        val indexDeleted = listOfViews.indexOf(customView)
        listOfViews.removeAt(indexDeleted)
        listOfUris.removeAt(indexDeleted)

        binding.customGridGroup.removeAllViews()
        listOfViews.clear()
        initCustomGrid(listOfUris)

    }

}