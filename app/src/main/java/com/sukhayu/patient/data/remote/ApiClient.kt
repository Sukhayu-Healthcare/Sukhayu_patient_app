package com.sukhayu.patient.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    const val BASE_URL = "https://sukhayu-backend.onrender.com/api/v1/"
    private const val BASE_URL_ANALYZE = "https://final-symptom-checker.onrender.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private fun getRetrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
    // Unified Lazy initialization with httpClient (and logging)
    val retrofit: ApiService by lazy {
        getRetrofit(BASE_URL).create(ApiService::class.java)
    }
    
    val apiServiceAnalyze: ApiService by lazy {
        getRetrofit(BASE_URL_ANALYZE).create(ApiService::class.java)
    }
}
