package com.quangln2.customfeed.data.datasource.local

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import com.quangln2.customfeed.data.constants.ConstantClass
import com.quangln2.customfeed.data.database.FeedDao
import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.others.utils.DownloadUtils.getMimeType
import com.quangln2.customfeed.others.utils.FileUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*

class LocalDataSourceImpl(private val feedDao: FeedDao) : LocalDataSource {
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun getFeedWithId(feed_id: String): MyPost = feedDao.getFeedWithId(feed_id)


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(myPost: MyPost) {
        if (feedDao.existsWithId(myPost.feedId) == 0) {
            feedDao.insert(myPost)
        } else {
            feedDao.update(myPost)
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun getAll(): List<MyPost> = feedDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun update(myPost: MyPost) = feedDao.update(myPost)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun delete(id: String) = feedDao.delete(id)


    override fun uploadMultipartBuilder(
        caption: String,
        uriLists: MutableList<Uri>,
        context: Context
    ): List<MultipartBody.Part> {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("feedId", UUID.randomUUID().toString())
        builder.addFormDataPart("name", ConstantClass.NAME)
        builder.addFormDataPart("avatar", ConstantClass.AVATAR_LINK)
        builder.addFormDataPart("createdTime", System.currentTimeMillis().toString())
        builder.addFormDataPart("caption", caption)

        for (uriItr in uriLists) {
            val tmp = FileUtils.getRealPathFromURI(uriItr, context)
            if (tmp != null) {
                val file = File(tmp)

                val requestFile = file.asRequestBody(getMimeType(file.toURI().toString())?.toMediaTypeOrNull())
                builder.addFormDataPart("upload", file.name, requestFile)
            }
        }
        return builder.build().parts
    }
}