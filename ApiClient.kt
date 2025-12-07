package com.example.app.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

data class FcmTokenRequest(val fcm_token: String)

interface ApiService {
    // ...existing code...
    @POST("device/register-token")
    fun registerFcmToken(
        @Header("Authorization") token: String,
        @Body body: FcmTokenRequest
    ): Call<GenericResponse>

    @GET("notices")
    fun getNotices(
        @Header("Authorization") token: String
    ): Call<List<Notice>>

    @GET("notice/{id}")
    fun getNoticeDetail(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<NoticeDetailResponse>
    // ...existing code...
}