package com.quangln2.customfeed.ui.customview

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.quangln2.customfeed.R

class CustomImageView {
    companion object {
        fun generateCustomImageView(context: Context, url: String): FrameLayout {
            val frameLayout = FrameLayout(context)
            val imageView = generateImageView(context, url)
            val crossButton = generateCrossButton(context)

            frameLayout.addView(imageView)
            frameLayout.addView(crossButton)

            return frameLayout
        }

        private fun generateCrossButton(context: Context): ImageView {
            val crossButton = ImageView(context)
            val params =
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)

            crossButton.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.remove_icon))
            params.gravity = Gravity.END
            crossButton.layoutParams = params
            crossButton.setPadding(16, 16, 16, 16)
            return crossButton
        }

        private fun generateImageView(context: Context, url: String): ImageView {
            val imageView = ImageView(context)
            imageView.layoutParams =
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(context).load(url.toUri()).into(imageView)
            return imageView
        }
    }


}