package com.quangln2.customfeedui.domain.workmanager

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.gson.Gson
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.controllers.FeedCtrl
import com.quangln2.customfeedui.data.database.FeedDatabase
import com.quangln2.customfeedui.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.models.others.EnumFeedSplashScreenState
import com.quangln2.customfeedui.data.models.others.UploadWorkerModel
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.domain.usecase.UploadPostV2UseCase
import com.quangln2.customfeedui.others.utils.FileUtils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*


class UploadService : Service() {
    val database by lazy {
        FeedDatabase.getFeedDatabase(this)
    }
    val builder = NotificationCompat.Builder(this, "FeedPost")
        .setContentTitle("CustomFeed")
        .setSmallIcon(R.drawable.ic_baseline_post_add_24)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    val id = UUID.randomUUID().toString().hashCode()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getStringExtra(resources.getString(R.string.jsonStringKey)) != null) {
            //Get data from intent
            val jsonString = intent.getStringExtra(resources.getString(R.string.jsonStringKey))
            if(jsonString != null){
                FeedCtrl.isLoadingToUpload.postValue(EnumFeedSplashScreenState.LOADING.value)

                val uploadWorkerModel = Gson().fromJson(jsonString, UploadWorkerModel::class.java)

                val uriLists = uploadWorkerModel.uriLists.map { it.toUri() }
                val uriListsForCompressing = if (uriLists.isEmpty()) emptyList() else FileUtils.compressImagesAndVideos(
                    uriLists.toMutableList(),
                    applicationContext
                )

                val uploadingPost = UploadPost.initializeUploadPost(applicationContext).copy(caption = uploadWorkerModel.caption)

                uploadFiles(uriListsForCompressing, uploadingPost, applicationContext)
            }
        }
        return START_NOT_STICKY
    }



    private fun uploadFiles(uriLists: List<Uri>, uploadingPost: UploadPost, context: Context){
        showLoadingUI(applicationContext)

        val listOfUrls = mutableListOf<String>()
        if(uriLists.isEmpty()){
            uploadToServer(uploadingPost)
            return
        }
        for(uri in uriLists){
            val filePath = "files/${uploadingPost.feedId}/${uri.path?.let { File(it).name }}"
            val mimeType = context.contentResolver.getType(uri)

            MediaManager.get().upload(uri)
                .option("public_id", filePath)
                .option("resource_type", if (mimeType?.contains("video") == true) "video" else "image")
                .callback(object: UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        Log.d("UploadServicePart", "onReschedule: ${error?.description}")
                        showWaitingUI(context)
                    }

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        Log.d("Cloudinary", resultData.toString())
                        resultData?.let {
                            Log.d("Cloudinary", "url: ${it["url"]} - width: ${it["width"]} - height: ${it["height"]}")

                            //Check before
                            if(listOfUrls.isEmpty()){
                                uploadingPost.firstWidth = it["width"] as Int
                                uploadingPost.firstHeight = it["height"] as Int
                            }

                            //Do by adding URL to list
                            listOfUrls.add(it["url"].toString())

                            //Check have enough data
                            if(listOfUrls.size == uriLists.size){
                                //Aggregate data
                                uploadingPost.imagesAndVideos = listOfUrls.toMutableList()

                                //Upload to server
                                uploadToServer(uploadingPost)
                            }
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.d("UploadServicePart", "onError: ${error?.description}")
                        showFailedUI(context, cause = error?.description.toString())
                        stopSelf()
                    }
                })
                .dispatch()


        }
    }
    private fun showWaitingUI(context: Context){
        builder.setContentText("Waiting for uploading...")
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }

    private fun showLoadingUI(context: Context){
        builder.apply {
            setProgress(0, 0, true)
            setContentText("Uploading...")
        }
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }


    private fun showSuccessfulUI(context: Context){
        FeedCtrl.isLoadingToUpload.postValue(EnumFeedSplashScreenState.COMPLETE.value)
        builder.apply {
            setProgress(0, 0, false)
            setContentText(resources.getString(R.string.upload_success))
            setAutoCancel(true)
        }
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }

        //show toast
        Toast.makeText(context, resources.getString(R.string.upload_success), Toast.LENGTH_LONG).show()

        stopSelf()
    }

    private fun showFailedUI(context: Context, cause: String = ""){
        //close loading card screen
        FeedCtrl.isLoadingToUpload.postValue(EnumFeedSplashScreenState.COMPLETE.value)

        //create notification
        builder.apply {
            setProgress(0, 0, false)
            setContentText("${resources.getString(R.string.upload_failed)}. $cause")
            setAutoCancel(true)
        }
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }

        //show toast
        Toast.makeText(context, "${resources.getString(R.string.upload_failed)}. $cause", Toast.LENGTH_LONG).show()

        stopSelf()
    }

    private fun uploadToServer(uploadingPost: UploadPost){
        val uploadPostV2UseCase = UploadPostV2UseCase(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
        uploadPostV2UseCase(uploadingPost).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    showSuccessfulUI(applicationContext)
                } else {
                    showFailedUI(applicationContext, cause = "Response code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showFailedUI(applicationContext, cause = t.cause.toString())
            }
        })
    }
}