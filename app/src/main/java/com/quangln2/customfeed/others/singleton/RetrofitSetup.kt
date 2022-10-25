package com.quangln2.customfeed.others.singleton

import com.quangln2.customfeed.data.constants.ConstantSetup
import com.quangln2.customfeed.data.datasource.remote.ApiSource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitSetup {
    val downloadClient = OkHttpClient()
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BASIC)
            }
        ).build()

    private val retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(ConstantSetup.DEFAULT_ENDPOINT)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val gitHubService: ApiSource = retrofit.create(ApiSource::class.java)


}
