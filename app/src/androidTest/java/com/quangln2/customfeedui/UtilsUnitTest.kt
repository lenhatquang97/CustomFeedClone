package com.quangln2.customfeedui

import android.content.Context
import android.webkit.URLUtil
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.quangln2.customfeedui.others.utils.DownloadUtils
import org.junit.After
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import java.io.File

class UtilsUnitTest {
    companion object{
        private lateinit var context: Context

        @BeforeClass
        @JvmStatic
        fun setup() {
            context = ApplicationProvider.getApplicationContext()
        }
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun validMimeType(){
        val url = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png"
        val mimeType = DownloadUtils.getMimeType(url)
        assert(mimeType == "image/png")
    }

    @Test
    fun testValidFileSizeFromInternet(){
        val validUrl = "https://res.cloudinary.com/ds72weirj/image/upload/v1667951132/cld-sample-5.jpg"
        val contentLength = 379132L
        val (result, error) = DownloadUtils.fileSizeFromInternet(validUrl)
        assert(result == contentLength && error == null)
    }

    @Test
    fun testInvalidFileSizeFromInternetBecauseOfNoContentLength(){
        val invalidUrl = "https://www.google.comee/"
        val (_, error) = DownloadUtils.fileSizeFromInternet(invalidUrl)
        assert(error != null)
    }

    @Test
    fun testDownloadVideo(){
        val validUrl = "https://sample-videos.com/video123/flv/720/big_buck_bunny_720p_10mb.flv"
        val (result, _) = DownloadUtils.fileSizeFromInternet(validUrl)
        DownloadUtils.downloadVideoSynchronous(validUrl, context)
        assert(DownloadUtils.isValidFile(validUrl, context, result))
    }

    @After
    fun deleteFile(){
        val validUrl = "https://sample-videos.com/video123/flv/720/big_buck_bunny_720p_10mb.flv"
        val fileName = URLUtil.guessFileName(validUrl, null, null)
        val file = File(context.cacheDir, fileName)
        if(file.exists()){
            file.delete()
        }

    }

}