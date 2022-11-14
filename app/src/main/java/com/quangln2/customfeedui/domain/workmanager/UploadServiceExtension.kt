package com.quangln2.customfeedui.domain.workmanager

import android.content.Context
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.quangln2.customfeedui.R
import com.quangln2.customfeedui.data.controllers.FeedCtrl
import com.quangln2.customfeedui.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeedui.data.models.datamodel.UploadPost
import com.quangln2.customfeedui.data.models.others.EnumFeedSplashScreenState
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.domain.usecase.UploadPostV2UseCase
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun UploadService.showSuccessfulUI(context: Context){
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
    Toast.makeText(context, resources.getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
}

fun UploadService.showFailedUI(context: Context, cause: String = ""){
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
    Toast.makeText(context, "${resources.getString(R.string.upload_failed)}. $cause", Toast.LENGTH_SHORT).show()
}

fun UploadService.uploadToServer(uploadingPost: UploadPost){
    val uploadPostV2UseCase = UploadPostV2UseCase(FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl()))
    uploadPostV2UseCase(uploadingPost).enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if(response.code() == 200){
                showSuccessfulUI(applicationContext)
                stopSelf()
            } else {
                showFailedUI(applicationContext, cause = "Response code: ${response.code()}")
                stopSelf()
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            showFailedUI(applicationContext, cause = t.cause.toString())
            stopSelf()
        }
    })
}