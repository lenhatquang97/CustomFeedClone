package com.quangln2.customfeedui.domain.workmanager

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.gson.Gson
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.controllers.FeedCtrl
import com.quangln2.customfeedui.data.database.FeedDatabase
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.models.others.EnumFeedSplashScreenState
import com.quangln2.customfeedui.data.models.others.UploadWorkerModel
import com.quangln2.customfeedui.others.utils.FileUtils
import java.io.File
import java.util.*


class UploadService : Service() {
    val database by lazy {
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
            //Get data from intent
            val jsonString = intent.getStringExtra(resources.getString(R.string.jsonStringKey))
            val uploadWorkerModel = Gson().fromJson(jsonString, UploadWorkerModel::class.java)

            val uriLists = uploadWorkerModel.uriLists.map { it.toUri() }
            val uriListsForCompressing = if (uriLists.isEmpty()) emptyList() else FileUtils.compressImagesAndVideos(
                uriLists.toMutableList(),
                applicationContext
            )

            val uploadingPost = UploadPost.initializeUploadPost(applicationContext).copy(caption = uploadWorkerModel.caption)

            FeedCtrl.isLoadingToUpload.postValue(EnumFeedSplashScreenState.LOADING.value)

            uploadFiles(uriListsForCompressing, uploadingPost)
        }
        return START_NOT_STICKY
    }

    private fun uploadFiles(uriLists: List<Uri>, uploadingPost: UploadPost){
        val listOfUrls = mutableListOf<String>()
        if(uriLists.isEmpty()){
            uploadToServer(uploadingPost)
            return
        }
        for(uri in uriLists){
            val filePath = "files/${uploadingPost.feedId}/${uri.path?.let { File(it).name }}"
            val mimeType = applicationContext.contentResolver.getType(uri)

            MediaManager.get().upload(uri)
                .option("public_id", filePath)
                .option("resource_type", if (mimeType?.contains("video") == true) "video" else "image")
                .callback(object: UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        Log.d("Cloudinary", resultData.toString())
                        resultData?.let {
                            Log.d("Cloudinary", "url: ${it["url"]} - width: ${it["width"]} - height: ${it["height"]}")

                            //Check before
                            if(listOfUrls.isEmpty()){
                                uploadingPost.firstWidth = it["width"] as Int
                                uploadingPost.firstHeight = it["height"] as Int
                            }

                            //Do this
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
                        showFailedUI(applicationContext, cause = error?.description.toString())
                        stopSelf()
                    }
                })
                .dispatch()
        }
    }
}