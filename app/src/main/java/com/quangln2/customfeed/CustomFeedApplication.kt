package com.quangln2.customfeed

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.cloudinary.android.MediaManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller


class CustomFeedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        updateAndroidSecurityProvider()
        MediaManager.init(this)
        createNotificationChannel()
    }

    private fun updateAndroidSecurityProvider() {
        try {
            ProviderInstaller.installIfNeeded(this)
            println("Installed security provider")
        } catch (e: GooglePlayServicesRepairableException) {
            // Thrown when Google Play Services is not installed, up-to-date, or enabled
            // Show dialog to allow users to install, update, or otherwise enable Google Play services.
            // IGNORE
        } catch (e: GooglePlayServicesNotAvailableException) {

        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(getString(R.string.channel_id), name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

