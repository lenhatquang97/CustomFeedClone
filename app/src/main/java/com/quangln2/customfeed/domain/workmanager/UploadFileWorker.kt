package com.quangln2.customfeed.domain.workmanager

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.quangln2.customfeed.R
import com.quangln2.customfeed.data.controllers.FeedController
import com.quangln2.customfeed.data.database.FeedDatabase
import com.quangln2.customfeed.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeed.data.models.UploadWorkerModel
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.domain.usecase.UploadMultipartBuilderUseCase
import com.quangln2.customfeed.domain.usecase.UploadPostUseCase
import com.quangln2.customfeed.others.utils.FileUtils
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.*

class UploadFileWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    private val database by lazy {
        FeedDatabase.getFeedDatabase(ctx)
    }
    val builder = NotificationCompat.Builder(ctx, ctx.getString(R.string.channel_id))
        .setContentTitle("CustomFeed")
        .setContentText("Uploading...")
        .setSmallIcon(R.drawable.ic_baseline_post_add_24)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    val id = UUID.randomUUID().toString().hashCode()

    override fun doWork(): Result {
        FeedController.isLoading.postValue(1)
        builder.setProgress(0, 0, true)
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(id, builder.build())
        }

        val jsonString = inputData.getString("jsonString")
        val uploadWorkerModel = Gson().fromJson(jsonString, UploadWorkerModel::class.java)

        val emptyList = mutableListOf<Uri>()
        val uploadMultipartBuilderUseCase = UploadMultipartBuilderUseCase(
            FeedRepository(
                LocalDataSourceImpl(database.feedDao()),
                RemoteDataSourceImpl()
            )
        )

        val uriLists = uploadWorkerModel.uriLists.map { it.toUri() }
        val uriListsForCompressing = if (uriLists.isEmpty()) emptyList else FileUtils.compressImagesAndVideos(
            uriLists.toMutableList(),
            applicationContext
        )

        val parts = uploadMultipartBuilderUseCase(uploadWorkerModel.caption, uriListsForCompressing, applicationContext)
        uploadFiles(parts, applicationContext)
        return Result.success()
    }

    private fun uploadFiles(requestBody: List<MultipartBody.Part>, context: Context) {
        val uploadPostUseCase = UploadPostUseCase(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
        uploadPostUseCase(requestBody).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.errorBody() == null && response.code() == 200) {
                    //close loading card screen
                    FeedController.isLoading.postValue(0)

                    //create notification
                    builder.apply {
                        setProgress(0, 0, false)
                        setContentText("Upload successfully")
                        setAutoCancel(true)
                    }
                    with(NotificationManagerCompat.from(context)) {
                        notify(id, builder.build())
                    }

                    //show toast
                    Toast.makeText(context, "Upload successfully", Toast.LENGTH_SHORT).show()

                } else {
                    //close loading card screen
                    FeedController.isLoading.postValue(0)

                    //create notification
                    builder.apply {
                        setProgress(0, 0, false)
                        setContentText("Upload failed")
                        setAutoCancel(true)
                    }
                    with(NotificationManagerCompat.from(context)) {
                        notify(id, builder.build())
                    }

                    //show toast
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()

                }


            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //close loading card screen
                FeedController.isLoading.postValue(0)

                //create notification
                builder.setProgress(0, 0, false)
                builder.setContentText("Upload failed")
                builder.setAutoCancel(true)
                with(NotificationManagerCompat.from(context)) {
                    notify(id, builder.build())
                }

                //show toast
                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()

            }
        })
    }
}