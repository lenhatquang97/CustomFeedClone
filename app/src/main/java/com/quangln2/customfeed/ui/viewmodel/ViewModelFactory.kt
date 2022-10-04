package com.quangln2.customfeed.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quangln2.customfeed.domain.DeleteFeedUseCase
import com.quangln2.customfeed.domain.GetAllFeedsUseCase
import com.quangln2.customfeed.domain.UploadMultipartBuilderUseCase
import com.quangln2.customfeed.domain.UploadPostUseCase
import com.quangln2.customfeed.data.repository.FeedRepository

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
                        UploadMultipartBuilderUseCase(feedRepository)
                    )
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}