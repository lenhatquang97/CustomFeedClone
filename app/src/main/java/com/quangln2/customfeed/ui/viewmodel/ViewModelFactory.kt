package com.quangln2.customfeed.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quangln2.customfeed.data.repository.FeedRepository
import com.quangln2.customfeed.domain.*

@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(private val feedRepository: FeedRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(FeedViewModel::class.java) ->
                    FeedViewModel(
                        UploadPostUseCase(feedRepository),
                        GetAllFeedsUseCase(feedRepository),
                        DeleteFeedUseCase(feedRepository),
                        UploadMultipartBuilderUseCase(feedRepository),
                        InsertDatabaseUseCase(feedRepository),
                        UpdateDatabaseUseCase(feedRepository),
                        DeleteDatabaseUseCase(feedRepository),
                        GetAllInDatabaseUseCase(feedRepository)
                    )
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}