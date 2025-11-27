package com.sukhayu.patient.data.remote

import com.sukhayu.patient.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val phone: String,      // FIXED → backend expects "phone"
    val password: String
)

data class LoginResponseAshaOrSupervisor(
    val message: String,
    val token: String,
    val role: String,
    val user: UserInfo
)

data class LoginResponsePatient(
    val message: String,
    val token: String,
    val role: String,
    val patient: PatientInfo,
    val familyProfiles: List<Any>? = null
)

interface ApiService {

   @POST("login")
    fun login(
        @Body body: LoginRequest
    ): Call<Map<String, Any>>

}
