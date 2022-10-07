package com.quangln2.customfeed.others.utils

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.quangln2.customfeed.data.models.MyPost

object CloudinaryUtils {
   fun uploadAnAsset(myPost: MyPost, uriString: String){
       val value = MediaManager.get().upload(Uri.parse(uriString))
           .option("folder", "files/${myPost.feedId}/")
           .dispatch()
   }
}