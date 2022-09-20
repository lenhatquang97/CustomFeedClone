package com.quangln2.customfeed.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.quangln2.customfeed.datasource.remote.RemoteDataSource
import com.quangln2.customfeed.models.UploadPost
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedViewModel : ViewModel() {
    var _uriLists = MutableLiveData<MutableList<Uri>>().apply { value = mutableListOf() }
    val uriLists: LiveData<MutableList<Uri>> = _uriLists

    private var _uploadLists = MutableLiveData<MutableList<UploadPost>>().apply { value = mutableListOf() }
    val uploadLists: LiveData<MutableList<UploadPost>> = _uploadLists

    private var _isUploading = MutableLiveData<Boolean>().apply { value = false }
    val isUploading: LiveData<Boolean> = _isUploading

    fun uploadFiles(requestBody: List<MultipartBody.Part>, context: Context) {
        _isUploading.value = true
        RemoteDataSource.uploadFiles(requestBody).enqueue(object : Callback<ResponseBody> {
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
        RemoteDataSource.getAllFeeds().enqueue(object : Callback<MutableList<UploadPost>> {
            override fun onResponse(call: Call<MutableList<UploadPost>>, response: Response<MutableList<UploadPost>>) {
                if (response.code() == 200) {
                    Log.d("GetAllFeeds", "Success")
                    val ls = mutableListOf<UploadPost>()
                    ls.add(UploadPost().copy(feedId = "none"))
                    ls.addAll(response.body()!!)
                    _uploadLists.value = ls.toMutableList()
                }
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

    fun deleteFeed(id: String) {
        RemoteDataSource.deleteFeed(id).enqueue(object : Callback<ResponseBody> {
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
}