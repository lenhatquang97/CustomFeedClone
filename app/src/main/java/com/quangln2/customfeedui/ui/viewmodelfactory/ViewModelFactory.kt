package com.quangln2.customfeedui.ui.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quangln2.customfeedui.data.repository.FeedRepository
import com.quangln2.customfeedui.domain.usecase.*
import com.quangln2.customfeedui.ui.viewmodel.FeedViewModel
import com.quangln2.customfeedui.ui.viewmodel.UploadViewModel
import com.quangln2.customfeedui.ui.viewmodel.ViewMoreViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(private val feedRepository: FeedRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(FeedViewModel::class.java) ->
                    FeedViewModel(
                        DeleteFeedUseCase(feedRepository),
                        GetAllFeedsModifiedUseCase(feedRepository)
                    )
                isAssignableFrom(UploadViewModel::class.java) ->
                    UploadViewModel()
                isAssignableFrom(ViewMoreViewModel::class.java) ->
                    ViewMoreViewModel(GetPostItemWithId(feedRepository))
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}