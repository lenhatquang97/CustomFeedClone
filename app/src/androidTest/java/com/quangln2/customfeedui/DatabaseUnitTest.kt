package com.quangln2.customfeedui

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.data.database.FeedDatabase
import com.quangln2.customfeedui.data.datasource.local.LocalDataSourceImpl
import com.quangln2.customfeedui.data.datasource.remote.RemoteDataSourceImpl
import com.quangln2.customfeedui.data.models.datamodel.MyPost
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.domain.usecase.*
import com.quangln2.customfeedui.ui.viewmodel.FeedViewModel
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*

class DatabaseUnitTest {
    companion object{
        private lateinit var context: Context
        private lateinit var database: FeedDatabase
        private lateinit var feedRepository: FeedRepository
        private lateinit var viewModel: FeedViewModel
        private const val databaseName = "test_database.db"

        @BeforeClass @JvmStatic
        fun setup() {
            context = ApplicationProvider.getApplicationContext()
            database = FeedDatabase.getFeedDatabase(context, databaseName)
            feedRepository = FeedRepository(LocalDataSourceImpl(database.feedDao()), RemoteDataSourceImpl())
        }

        @AfterClass @JvmStatic
        fun tearDown(){
            database.close()
        }
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()



    @Before
    fun setupViewModelAndAddItemToDatabase() = runBlockingTest {
        viewModel = FeedViewModel(
            GetAllFeedsUseCase(feedRepository),
            DeleteFeedUseCase(feedRepository),
            InsertDatabaseUseCase(feedRepository),
            DeleteDatabaseUseCase(feedRepository),
            GetAllInDatabaseUseCase(feedRepository)
        )

        val item = MyPost().copy(
            name = "Quang Le",
            avatar = ConstantSetup.AVATAR_LINK,
            createdTime = System.currentTimeMillis().toString(),
            caption = "Hello world @123",
        )
        viewModel.insertDatabaseUseCase(item)
    }

    //Test case 1: Add item in DB
    @Test
    fun addItemInDatabase() = runBlockingTest {
        val list = viewModel.getAllInDatabaseUseCase()
        val filteredList = list.filter { it.name == "Quang Le" && it.caption == "Hello world @123" }
        assert(filteredList.size == 1)
    }

    //Test case 2: Delete item in DB
    @Test
    fun deleteItemInDatabase() = runBlockingTest {
        val list = viewModel.getAllInDatabaseUseCase()
        val filteredList = list.filter { it.name == "Quang Le" && it.caption == "Hello world @123" }
        viewModel.deleteDatabaseUseCase(filteredList[0].feedId)
        val listAfterDelete = viewModel.getAllInDatabaseUseCase()
        val filteredListAfterDelete = listAfterDelete.filter { it.name == "Quang Le" && it.caption == "Hello world @123" }
        assert(filteredListAfterDelete.isEmpty())
    }

    //Test case 3: Update item in DB
    @Test
    fun updateItemInDatabase() = runBlockingTest {
        val listInserted = viewModel.getAllInDatabaseUseCase()
        val itr = listInserted.filter { it.caption == "Hello world @123" }

        if(itr.size == 1){
            itr[0].caption = "Hello world 2"
            viewModel.insertDatabaseUseCase(itr[0])

            val listUpdated = viewModel.getAllInDatabaseUseCase()
            val itr2 = listUpdated.filter { it.caption == "Hello world 2" }
            viewModel.deleteDatabaseUseCase(itr2[0].feedId)

            assert(itr2.size == 1 && itr2[0].caption == "Hello world 2")
        } else assert(false)
    }

    @After
    fun removeItemFromDatabase() = runBlockingTest{
        val list = viewModel.getAllInDatabaseUseCase()
        val filteredList = list.filter { it.name == "Quang Le" && it.caption == "Hello world @123" }
        for(item in filteredList){
            viewModel.deleteDatabaseUseCase(item.feedId)
        }
    }



}