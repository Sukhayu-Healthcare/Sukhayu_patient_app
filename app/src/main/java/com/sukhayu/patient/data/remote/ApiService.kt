package com.sukhayu.patient.data.remote

import com.sukhayu.patient.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val patient_phone: String,
    val password: String
)

interface ApiService {

    @POST("patient/login")
    fun loginPatient(
        @Body body: LoginRequest
    ): Call<LoginResponse>
}
