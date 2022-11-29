package com.quangln2.customfeedui.others.binding

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

object BindingAdapters {
    @BindingAdapter("android:visibility")
    @JvmStatic
    fun setVisibility(view: View, visible: Int){
        view.visibility = visible
    }

    @BindingAdapter("app:isRefreshing")
    @JvmStatic
    fun setIsRefreshing(view: SwipeRefreshLayout, state: Boolean){
        view.isRefreshing = state
    }
}