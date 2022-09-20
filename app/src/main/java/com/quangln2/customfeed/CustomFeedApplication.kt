package com.quangln2.customfeed

import android.app.Application
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller


class CustomFeedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        updateAndroidSecurityProvider()
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
}

