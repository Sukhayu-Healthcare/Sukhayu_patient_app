package com.sukhayu.patient.data.remote

import com.sukhayu.patient.data.remote.AshaListResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

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
