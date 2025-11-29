package com.sukhayu.patient.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // ✅ Added trailing slash - REQUIRED by Retrofit
    private const val BASE_URL = "https://sukhayu-backend.onrender.com/api/v1/"

    val retrofit: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
