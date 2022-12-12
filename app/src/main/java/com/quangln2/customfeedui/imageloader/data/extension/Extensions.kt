package com.quangln2.customfeedui.imageloader.data.extension

import android.graphics.Rect
import android.widget.ImageView
import com.quangln2.customfeedui.data.constants.ConstantSetup
import com.quangln2.customfeedui.imageloader.data.memcache.LruBitmapCache

fun ImageView.recycle(){
    val imageViewAddress = this.hashCode().toString()
    if(!checkViewIsVisible()){
        if(LruBitmapCache.bitmapToImageViewAddress.containsKey(imageViewAddress)){
            val value = LruBitmapCache.bitmapToImageViewAddress[imageViewAddress]
            if (value != null) {
                LruBitmapCache.bitmapToImageViewAddress.remove(value)
                LruBitmapCache.removeCache(value)
            }
        }
    }

}

fun ImageView.checkViewIsVisible(): Boolean{
    val currentViewRect = Rect()
    this.getLocalVisibleRect(currentViewRect)
    val height = currentViewRect.height()
    val isOutOfBoundsOnTheTop = currentViewRect.bottom < 0 && currentViewRect.top < 0
    val isOutOfBoundsAtTheBottom =
        currentViewRect.top >= ConstantSetup.PHONE_HEIGHT && currentViewRect.bottom >= ConstantSetup.PHONE_HEIGHT
    return if (isOutOfBoundsAtTheBottom || isOutOfBoundsOnTheTop) {
        false
    } else {
        val tmp = this.height
        val percents = height * 100 / if(tmp == 0) 1 else tmp
        percents >= 50
    }
}

fun ImageView.addToManagedAddress(webUrlOrFileUri: String){
    val imageViewAddress = this.hashCode().toString()
    if(checkViewIsVisible()){
        LruBitmapCache.bitmapToImageViewAddress[imageViewAddress] = webUrlOrFileUri
    }

}