package com.quangln2.customfeed.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.quangln2.customfeed.data.models.UploadPost
import com.quangln2.customfeed.domain.DeleteFeedUseCase
import com.quangln2.customfeed.domain.GetAllFeedsUseCase
import com.quangln2.customfeed.domain.UploadMultipartBuilderUseCase
import com.quangln2.customfeed.domain.UploadPostUseCase
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedViewModel(
    val uploadPostUseCase: UploadPostUseCase,
    val getAllFeedsUseCase: GetAllFeedsUseCase,
    val deleteFeedUseCase: DeleteFeedUseCase,
    val uploadMultipartBuilderUseCase: UploadMultipartBuilderUseCase
) : ViewModel() {
    var _uriLists = MutableLiveData<MutableList<Uri>>().apply { value = mutableListOf() }
    val uriLists: LiveData<MutableList<Uri>> = _uriLists

    private var _uploadLists = MutableLiveData<MutableList<UploadPost>>().apply { value = mutableListOf() }
    val uploadLists: LiveData<MutableList<UploadPost>> = _uploadLists

    private var _isUploading = MutableLiveData<Boolean>().apply { value = false }
    val isUploading: LiveData<Boolean> = _isUploading

    fun uploadFiles(requestBody: List<MultipartBody.Part>, context: Context) {
        _isUploading.value = true
        uploadPostUseCase(requestBody).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    println(response.body())
                    Log.d("UploadFile", "Success")
                    Toast.makeText(context, "Upload Success", Toast.LENGTH_SHORT).show()
                    _isUploading.value = false
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("UploadFile", "Failure")
                println(t.cause?.message)
            }

        })
    }

    fun getAllFeeds() {
        getAllFeedsUseCase().enqueue(object : Callback<MutableList<UploadPost>> {
            override fun onResponse(call: Call<MutableList<UploadPost>>, response: Response<MutableList<UploadPost>>) {
                val ls = mutableListOf<UploadPost>()
                if (response.code() == 200) {
                    Log.d("GetAllFeeds", "Success")
                    ls.add(UploadPost().copy(feedId = "none"))
                    ls.addAll(response.body()!!)
                } else {
                    ls.add(UploadPost().copy(feedId = "none"))
                }
                _uploadLists.value = ls.toMutableList()
            }

            override fun onFailure(call: Call<MutableList<UploadPost>>, t: Throwable) {
                Log.d("GetAllFeeds", "Failure")
                println(t.cause?.message)
                val ls = mutableListOf<UploadPost>()
                ls.add(UploadPost().copy(feedId = "none"))
                _uploadLists.value = ls.toMutableList()
            }

        })
    }

    fun getFeedItem(feedId: String): UploadPost {
        val ls = _uploadLists.value
        if (ls != null) {
            val indexOfFirst = ls.indexOfFirst { it.feedId == feedId }
            return ls[indexOfFirst]
        }
        return UploadPost().copy(feedId = "none")

    }

    fun deleteFeed(id: String) {
        deleteFeedUseCase(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    val ls = uploadLists.value
                    ls?.removeIf { it.feedId == id }
                    _uploadLists.value = ls?.toMutableList()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("DeleteFeed", "Failure")
                println(t.cause?.message)
            }

        })
    }

    fun uploadMultipartBuilder(
        caption: String,
        uriLists: LiveData<MutableList<Uri>>,
        context: Context
    ): List<MultipartBody.Part> {
        return uploadMultipartBuilderUseCase(caption, uriLists, context)
    }
}