package com.quangln2.customfeedui.domain.workmanager

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.controllers.FeedCtrl
import com.quangln2.customfeedui.data.database.FeedDatabase
import com.quangln2.customfeedui.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.models.others.EnumFeedSplashScreenState
import com.quangln2.customfeedui.data.models.others.UploadWorkerModel
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.domain.usecase.UploadFileWithPostIdUseCase
import com.quangln2.customfeedui.domain.usecase.UploadPostV2UseCase
import com.quangln2.customfeedui.extensions.getImageDimensions
import com.quangln2.customfeedui.extensions.getVideoSize
import com.quangln2.customfeedui.extensions.md5
import com.quangln2.customfeedui.others.utils.DownloadUtils
import com.quangln2.customfeedui.others.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*


class UploadService : Service() {
    val database by lazy {
        FeedDatabase.getFeedDatabase(this)
    }
    private val builder = NotificationCompat.Builder(this, "FeedPost")
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

                val uploadWorkerModel = UploadWorkerModel.jsonStringToUploadWorker(jsonString)

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
        val feedRepository = FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl())
        val uploadFileWithPostIdUseCase = UploadFileWithPostIdUseCase(feedRepository)

        CoroutineScope(Dispatchers.IO).launch {
            for(uri in uriLists){
                val actualUri = handleCaseUri(uri)
                val md5Checksum = actualUri.md5()
                val actualUrl = uploadFileWithPostIdUseCase(ConstantSetup.UPLOAD_FILE, actualUri, uploadingPost.feedId, md5Checksum)
                if(actualUrl != "error"){
                    if(listOfUrls.isEmpty()){
                        val mimeType = DownloadUtils.getMimeType(uri.path)
                        if(mimeType?.contains("image") == true){
                            val (width, height) = uri.getImageDimensions(context)
                            uploadingPost.firstWidth = width
                            uploadingPost.firstHeight = height
                        } else {
                            //That means mimeType is video
                            val (width, height) = uri.getVideoSize(context)
                            uploadingPost.firstWidth = width
                            uploadingPost.firstHeight = height
                        }
                    }
                    //Do by adding URL to list
                    listOfUrls.add(actualUrl)

                    //Check have enough data
                    if(listOfUrls.size == uriLists.size){
                        //Aggregate data
                        uploadingPost.imagesAndVideos = listOfUrls.toMutableList()

                        //Upload to server
                        uploadToServer(uploadingPost)
                    }
                } else {
                    withContext(Dispatchers.Main){
                        showFailedUI(context, cause = "Error when uploading files")
                    }
                    stopSelf()
                }
            }
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
        FeedCtrl.isLoadingToUpload.value = EnumFeedSplashScreenState.COMPLETE.value
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
        FeedCtrl.isLoadingToUpload.value = EnumFeedSplashScreenState.COMPLETE.value

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
        val responseCode = uploadPostV2UseCase(uploadingPost)
        if(responseCode == 200){
            CoroutineScope(Dispatchers.Main).launch {
                showSuccessfulUI(applicationContext)
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                showFailedUI(applicationContext, cause = "Response code: $responseCode")
            }

        }
    }
    private fun handleCaseUri(uri: Uri): File {
        if(uri.scheme == "content"){
            val cursor = contentResolver.query(uri, arrayOf(MediaStore.Video.VideoColumns.DATA), null, null, null)
            cursor?.moveToFirst()
            val path = cursor?.getString(0)
            cursor?.close()
            return File(path)
        }
        return uri.toFile()
    }
}