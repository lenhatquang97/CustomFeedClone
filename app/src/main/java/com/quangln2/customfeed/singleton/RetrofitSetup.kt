package com.quangln2.customfeed.singleton

import com.quangln2.customfeed.datasource.remote.ApiSource
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitSetup {
    private const val DEFAULT_ENDPOINT = "http://192.168.14.199:8080"
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BASIC)
            }
        ).connectionSpecs(
            listOf(
                ConnectionSpec.CLEARTEXT
            )
        ).build()

    private val retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(DEFAULT_ENDPOINT)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val gitHubService = retrofit.create(ApiSource::class.java)


}
