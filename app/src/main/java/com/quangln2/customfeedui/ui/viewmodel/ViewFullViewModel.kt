package com.quangln2.customfeedui.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewFullViewModel: ViewModel() {
    val fullVideoViewVisibility = MutableLiveData(true)
    val fullVideoProgressBarVisibility = MutableLiveData(false)
    val fullImageViewVisibility = MutableLiveData(false)

}