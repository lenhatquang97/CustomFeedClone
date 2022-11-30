package com.quangln2.customfeedui.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.models.uimodel.MyPostRender
import com.quangln2.customfeedui.domain.usecase.GetPostItemWithId
import com.quangln2.customfeedui.others.utils.DownloadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewMoreViewModel(private val getPostItemWithId: GetPostItemWithId): ViewModel() {
    private var _postItem = MutableLiveData<MyPost>()
    val postItem: LiveData<MyPost> = _postItem
    fun getPostWithId(id: String){
        viewModelScope.launch(Dispatchers.IO) {
            val tmp = getPostItemWithId(id)
            _postItem.postValue(tmp)
        }
    }
    fun retrieveValueAndMimeType(item: MyPostRender, index: Int, context: Context): Pair<String, String?>{
        val value = DownloadUtils.getTemporaryFilePath(
            item.resources[index].url,
            context,
            item.resources[index].size
        )
        val mimeType = DownloadUtils.getMimeType(value)
        return Pair(value, mimeType)
    }
}