package com.quangln2.customfeed

import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.quangln2.customfeed.databinding.ActivityMainBinding
import com.quangln2.customfeed.others.utils.FileUtils

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        viewBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        FileUtils.getPermissionForStorage(applicationContext, this)
    }
}