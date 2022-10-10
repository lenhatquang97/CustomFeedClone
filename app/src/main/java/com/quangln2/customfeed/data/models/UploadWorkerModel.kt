package com.quangln2.customfeed.data.models

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.quangln2.customfeed.domain.workmanager.UploadFileWorker

data class UploadWorkerModel(
    @SerializedName("caption")
    @Expose
    val caption: String,

    @SerializedName("uriLists")
    @Expose
    val uriLists: List<String>
)

fun sendTaskToWorkManager(caption: String, uriLists: MutableList<Uri>, context: Context){
    val uriStringLists = uriLists.map { it.toString() }
    val uploadWorkerModel = UploadWorkerModel(caption, uriStringLists)

    val jsonString = Gson().toJson(uploadWorkerModel)
    val inputData = Data.Builder().putString("jsonString", jsonString).build()

    val oneTimeWorkRequest = OneTimeWorkRequest.Builder(UploadFileWorker::class.java)
        .setInputData(inputData)
        .build()

    val workManager = WorkManager.getInstance(context)
    workManager.enqueue(oneTimeWorkRequest)

}