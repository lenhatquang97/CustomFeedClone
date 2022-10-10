package com.quangln2.customfeed.domain.workmanager

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.quangln2.customfeed.data.database.FeedDatabase
import com.quangln2.customfeed.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeed.data.models.UploadWorkerModel
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.domain.UploadMultipartBuilderUseCase
import com.quangln2.customfeed.domain.UploadPostUseCase
import com.quangln2.customfeed.others.utils.FileUtils
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class UploadFileWorker(ctx: Context, params: WorkerParameters): Worker(ctx, params) {
    private val database by lazy {
        FeedDatabase.getFeedDatabase(ctx)
    }

    override fun doWork(): Result {
        val jsonString = inputData.getString("jsonString")
        val uploadWorkerModel = Gson().fromJson(jsonString, UploadWorkerModel::class.java)

        val emptyList = MutableLiveData<MutableList<Uri>>().apply { value = mutableListOf() }
        val uploadMultipartBuilderUseCase = UploadMultipartBuilderUseCase(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))

        val uriLists = uploadWorkerModel.uriLists.map { it.toUri() }
        val uriListsForCompressing = if(uriLists.isEmpty()) emptyList else FileUtils.compressImagesAndVideos(uriLists.toMutableList(), applicationContext)

        val parts = uploadMultipartBuilderUseCase(uploadWorkerModel.caption, uriListsForCompressing, applicationContext)

        return try{
            uploadFiles(parts, applicationContext)
            Result.success()
        } catch (e: Exception){
            e.printStackTrace()
            Result.failure()
        }
    }

    fun uploadFiles(requestBody: List<MultipartBody.Part>, context: Context) {
        val uploadPostUseCase = UploadPostUseCase(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
        uploadPostUseCase(requestBody).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("UploadFile", "Success")

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("UploadFile", "Failure")
            }
        })
    }
}