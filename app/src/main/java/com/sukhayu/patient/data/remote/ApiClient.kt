package com.sukhayu.patient.data.remote

import com.sukhayu.patient.data.remote.AshaListResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {

    // ✅ Added trailing slash - REQUIRED by Retrofit
    const val BASE_URL = "https://sukhayu-backend.onrender.com/api/v1/"
    private const val BASE_URL_ANALYZE = "https://ai-symptom-checker-p7f7.onrender.com/"

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
            
    val retrofit: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    val apiServiceAnalyze: ApiService = getRetrofit(BASE_URL_ANALYZE).create(ApiService::class.java)
}
