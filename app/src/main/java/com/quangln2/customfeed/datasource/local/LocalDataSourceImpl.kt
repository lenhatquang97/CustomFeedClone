package com.quangln2.customfeed.datasource.local

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.quangln2.customfeed.constants.ConstantClass
import com.quangln2.customfeed.utils.FileUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*

class LocalDataSourceImpl : LocalDataSource {
    override fun uploadMultipartBuilder(
        caption: String,
        uriLists: LiveData<MutableList<Uri>>,
        context: Context
    ): List<MultipartBody.Part> {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("feedId", UUID.randomUUID().toString())
        builder.addFormDataPart("name", ConstantClass.NAME)
        builder.addFormDataPart("avatar", ConstantClass.AVATAR_LINK)
        builder.addFormDataPart("createdTime", System.currentTimeMillis().toString())
        builder.addFormDataPart("caption", caption)

        for (uriItr in uriLists.value!!) {
            val tmp = FileUtils.getRealPathFromURI(uriItr, context)
            if (tmp != null) {
                val file = File(tmp)
                val requestFile = RequestBody.create(
                    if (uriItr.toString()
                            .contains("mp4")
                    ) "video/*".toMediaTypeOrNull() else "image/*".toMediaTypeOrNull(), file
                )
                builder.addFormDataPart("upload", file.name, requestFile)
            }
        }
        return builder.build().parts
    }
}