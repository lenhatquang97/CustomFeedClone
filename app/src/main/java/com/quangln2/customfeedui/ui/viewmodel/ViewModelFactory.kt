package com.quangln2.customfeedui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.domain.usecase.*

@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(private val feedRepository: FeedRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(FeedViewModel::class.java) ->
                    FeedViewModel(
                        GetAllFeedsUseCase(feedRepository),
                        DeleteFeedUseCase(feedRepository),
                        InsertDatabaseUseCase(feedRepository),
                        DeleteDatabaseUseCase(feedRepository),
                        GetAllInDatabaseUseCase(feedRepository)
                    )
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}