package com.quangln2.customfeed

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quangln2.customfeed.data.database.FeedDatabase
import com.quangln2.customfeed.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeed.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeed.data.models.datamodel.MyPost
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.domain.usecase.*
import com.quangln2.customfeed.others.utils.DownloadUtils
import com.quangln2.customfeed.ui.viewmodel.FeedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private lateinit var context: Context
    private lateinit var database: FeedDatabase
    private lateinit var feedRepository: FeedRepository
    private lateinit var viewModel: FeedViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup(){
        context = ApplicationProvider.getApplicationContext<Context>()
        database = FeedDatabase.getFeedDatabase(context)
        feedRepository = FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl())
        viewModel = FeedViewModel(
            GetAllFeedsUseCase(feedRepository),
            DeleteFeedUseCase(feedRepository),
            InsertDatabaseUseCase(feedRepository),
            DeleteDatabaseUseCase(feedRepository),
            GetAllInDatabaseUseCase(feedRepository)
        )
    }

    @After
    fun tearDown(){
        database.close()
    }

    private suspend fun retrieveInDatabase(): List<MyPost>{
        return viewModel.getAllInDatabaseUseCase()
    }

    private fun hasMimeTypeContains(url: String, subString: String): Boolean{
        val mimeType = DownloadUtils.getMimeType(url)
        return mimeType!= null && mimeType.contains(subString)
    }

    @Test
    fun getWhetherHaveEnoughDataForDemo() = runBlockingTest {
        val result = retrieveInDatabase()
        var hasMoreThan10 = false
        var hasMoreThanTwoVideos = false
        var hasLongText = false
        var hasMoreImages = false
        var hasBothImagesAndVideos = false

        result.forEach {
            val numsOfVideo = it.resources.filter { itr -> hasMimeTypeContains(itr.url, "video") }.size
            val numsOfImage = it.resources.filter { itr -> hasMimeTypeContains(itr.url, "image") }.size
            if(it.resources.size >= 10){
                hasMoreThan10 = true
            }

            if(it.caption.length >= 50){
                hasLongText = true
            }

            if(numsOfVideo > 0 && numsOfImage > 0){
                hasBothImagesAndVideos = true
            } else if(numsOfVideo >= 2){
                hasMoreThanTwoVideos = true
            } else if(numsOfImage >= 2){
                hasMoreImages = true
            }
        }


        assert(hasMoreThan10 && hasMoreThanTwoVideos && hasLongText && hasMoreImages && hasBothImagesAndVideos)
    }

}