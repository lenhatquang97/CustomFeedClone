package com.quangln2.customfeedui.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.quangln2.customfeedui.data.models.others.UploadWorkerModel
import com.quangln2.customfeedui.data.models.uimodel.ItemLocation
import com.quangln2.customfeedui.domain.workmanager.UploadService
import com.quangln2.customfeedui.others.extensions.getImageDimensions
import com.quangln2.customfeedui.ui.customview.customgrid.getGridItemsLocation
import com.quangln2.customfeedui.ui.screens.addpost.IMAGE_MIMETYPE
import com.quangln2.customfeedui.ui.screens.addpost.VIDEO_MIMETYPE

class UploadViewModel: ViewModel() {
    private var _uriLists = MutableLiveData<MutableList<Uri>>()
    val uriLists: LiveData<MutableList<Uri>> = _uriLists
    init {
        _uriLists.apply { value = mutableListOf() }
    }

    fun clearImageAndVideoGrid() = _uriLists.value?.clear()
    fun addImageAndVideoGridInBackground(ls: MutableList<Uri>?) = _uriLists.postValue(ls?.toMutableList())

    fun uploadFiles(caption: String, uriLists: MutableList<Uri>, context: Context) {
        val uriStringLists = uriLists.map { it.toString() }
        val uploadWorkerModel = UploadWorkerModel(caption, uriStringLists)
        val jsonString = Gson().toJson(uploadWorkerModel)
        val intent = Intent(context, UploadService::class.java)
        intent.putExtra("jsonString", jsonString)
        context.startService(intent)
    }

    fun initializeDataForShowingGrid(listOfUris: MutableList<Uri>, viewSize: Int, context: Context): List<ItemLocation> {
        val pair = if (listOfUris.isNotEmpty()) listOfUris[0].getImageDimensions(context) else Pair(0, 0)
        val rectangles = getGridItemsLocation(viewSize, pair.first, pair.second)

        val marginLeft = 8
        val contentPadding = 32
        val marginHorizontalSum = 2 * marginLeft + contentPadding
        val widthGrid = Resources.getSystem().displayMetrics.widthPixels - marginHorizontalSum

        val itemLocations = mutableListOf<ItemLocation>()
        for (i in rectangles.indices) {
            val leftView = (rectangles[i].leftTop.x * widthGrid).toInt() + contentPadding
            val topView = (rectangles[i].leftTop.y * widthGrid).toInt() + contentPadding
            val widthView = (rectangles[i].rightBottom.x * widthGrid).toInt() - (rectangles[i].leftTop.x * widthGrid).toInt() - contentPadding
            val heightView = (rectangles[i].rightBottom.y * widthGrid).toInt() - (rectangles[i].leftTop.y * widthGrid).toInt() - contentPadding
            val itemLocation = ItemLocation(leftView, topView, widthView, heightView)
            itemLocations.add(itemLocation)
        }
        return itemLocations
    }
    fun handleChooseImagesOrVideos(resultLauncher: ActivityResultLauncher<Intent>){
        val pickerIntent = Intent(Intent.ACTION_PICK)
        pickerIntent.apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(IMAGE_MIMETYPE, VIDEO_MIMETYPE))
        }
        resultLauncher.launch(pickerIntent)
    }
}