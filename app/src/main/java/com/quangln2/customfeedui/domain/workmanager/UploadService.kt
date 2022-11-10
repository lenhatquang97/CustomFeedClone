package com.quangln2.customfeedui.domain.workmanager

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.google.gson.Gson
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.controllers.FeedCtrl
import com.quangln2.customfeedui.data.database.FeedDatabase
import com.quangln2.customfeedui.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeedui.data.models.others.EnumFeedSplashScreenState
import com.quangln2.customfeedui.data.models.others.UploadWorkerModel
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.domain.usecase.UploadMultipartBuilderUseCase
import com.quangln2.customfeedui.domain.usecase.UploadPostUseCase
import com.quangln2.customfeedui.others.utils.FileUtils
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.*

class UploadService : Service() {
    private val database by lazy {
        FeedDatabase.getFeedDatabase(this)
    }
    val builder = NotificationCompat.Builder(this, "FeedPost")
        .setContentTitle("CustomFeed")
        .setContentText("Uploading...")
        .setSmallIcon(R.drawable.ic_baseline_post_add_24)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    val id = UUID.randomUUID().toString().hashCode()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        builder.setProgress(0, 0, true)
        startForeground(1, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            FeedCtrl.isLoadingToUpload.postValue(EnumFeedSplashScreenState.LOADING.value)
            val jsonString = intent.getStringExtra(resources.getString(R.string.jsonStringKey))
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

            val parts =
                uploadMultipartBuilderUseCase(uploadWorkerModel.caption, uriListsForCompressing, applicationContext)
            uploadFiles(parts, applicationContext)

        }
        return START_NOT_STICKY
    }

    private fun uploadFiles(requestBody: List<MultipartBody.Part>, context: Context) {
        val uploadPostUseCase =
            UploadPostUseCase(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
        uploadPostUseCase(requestBody).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.errorBody() == null && response.code() == 200) {
                    //close loading card screen
                    FeedCtrl.isLoadingToUpload.postValue(EnumFeedSplashScreenState.COMPLETE.value)

                    //create notification
                    builder.apply {
                        setProgress(0, 0, false)
                        setContentText(resources.getString(R.string.upload_success))
                        setAutoCancel(true)
                    }
                    with(NotificationManagerCompat.from(context)) {
                        notify(id, builder.build())
                    }

                    //show toast
                    Toast.makeText(context, resources.getString(R.string.upload_success), Toast.LENGTH_SHORT).show()

                } else {
                    //close loading card screen
                    FeedCtrl.isLoadingToUpload.postValue(EnumFeedSplashScreenState.COMPLETE.value)

                    //create notification
                    builder.apply {
                        setProgress(0, 0, false)
                        setContentText(resources.getString(R.string.upload_failed))
                        setAutoCancel(true)
                    }
                    with(NotificationManagerCompat.from(context)) {
                        notify(id, builder.build())
                    }

                    //show toast
                    Toast.makeText(context, resources.getString(R.string.upload_failed), Toast.LENGTH_SHORT).show()

                }

                stopSelf()


            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //close loading card screen
                FeedCtrl.isLoadingToUpload.postValue(EnumFeedSplashScreenState.COMPLETE.value)

                //create notification
                builder.setProgress(0, 0, false)
                builder.setContentText(resources.getString(R.string.upload_failed))
                builder.setAutoCancel(true)
                with(NotificationManagerCompat.from(context)) {
                    notify(id, builder.build())
                }

                //show toast
                Toast.makeText(context, resources.getString(R.string.upload_failed), Toast.LENGTH_SHORT).show()
                stopSelf()

            }
        })
    }

}