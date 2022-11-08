package com.quangln2.customfeed

import android.net.ConnectivityManager
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.quangln2.customfeed.databinding.ActivityMainBinding
import com.quangln2.customfeed.others.utils.FileUtils

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private val connectivityManager by lazy {
        getSystemService(ConnectivityManager::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        viewBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        FileUtils.getPermissionForStorage(applicationContext, this)

        connectivityManager.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    super.onAvailable(network)
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        resources.getString(R.string.internet_connected),
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }

                override fun onLost(network: android.net.Network) {
                    super.onLost(network)
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        resources.getString(R.string.offline_mode),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        )

    }


}