package com.quangln2.customfeed.ui.screens.addpost

import android.net.Uri
import android.provider.MediaStore
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.quangln2.customfeed.data.constants.ConstantClass
import com.quangln2.customfeed.others.utils.FileUtils
import com.quangln2.customfeed.ui.customview.CustomLayer

fun HomeScreenFragment.loadInitialProfile(){
    // Load initial avatar
    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).override(100)
    Glide.with(requireContext()).load(ConstantClass.AVATAR_LINK).apply(requestOptions).into(binding.myAvatarImage)
}

fun HomeScreenFragment.getFirstImageWidthAndHeight(i: Int, uri: Uri) {
    if (i == 0) {
        var bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
//        binding.customGridGroup.firstItemWidth = bitmap.getScaledWidth(DisplayMetrics.DENSITY_DEFAULT)
//        binding.customGridGroup.firstItemHeight = bitmap.getScaledHeight(DisplayMetrics.DENSITY_DEFAULT)
        bitmap.recycle()
    }
}
fun HomeScreenFragment.getFirstVideoWidthAndHeight(i: Int, uri: Uri) {
    if (i == 0) {
        val drawable = FileUtils.getVideoThumbnail(uri, requireContext())
//        binding.customGridGroup.firstItemWidth = drawable.intrinsicWidth
//        binding.customGridGroup.firstItemHeight = drawable.intrinsicHeight
    }
}

fun HomeScreenFragment.onHandleMoreImagesOrVideos(customView: View) {
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

            binding.customGridGroup.addView(listOfViews[index - 1])


            val view = CustomLayer(requireContext())
            view.addedImagesText.text = "+${textValue - 1}"
            binding.customGridGroup.addView(view)
            if (listOfViews.size >= 10) {
                listOfViews.add(8, CustomLayer(requireContext()))
                listOfUris.add(8, Uri.EMPTY)
            }


        }
    }
}

fun HomeScreenFragment.hasCustomLayer(): Pair<Int, Int> {
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

fun HomeScreenFragment.preUploadFiles(): Pair <MutableList<Uri>, String> {
    val mutableLists = mutableListOf<Uri>()
    for (i in 0 until listOfUris.size) {
        if (listOfUris[i] != Uri.EMPTY) {
            mutableLists.add(listOfUris[i])
        }
    }
    val caption = binding.textField.editText?.text.toString()
    return Pair(mutableLists, caption)
}