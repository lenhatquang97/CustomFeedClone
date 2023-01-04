package com.quangln2.customfeedui

import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.quangln2.customfeedui.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)
        supportActionBar?.hide()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }
}