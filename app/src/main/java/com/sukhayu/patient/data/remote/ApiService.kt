package com.sukhayu.patient.data.remote

import com.sukhayu.patient.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val phone: String,      // FIXED → backend expects "phone"
    val password: String
)

data class UserInfo(
    val id: String,
    val name: String,
    val phone: String
)

data class PatientInfo(
    val id: String,
    val name: String,
    val phone: String,
    val supreme_id: String
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

    @POST("patient/login")
    fun loginPatient(
        @Body body: LoginRequest
    ): Call<LoginResponsePatient>

    @POST("asha/login")
    fun loginAsha(
        @Body body: LoginRequest
    ): Call<LoginResponseAshaOrSupervisor>

    @POST("supervisor/login")
    fun loginSupervisor(
        @Body body: LoginRequest
    ): Call<LoginResponseAshaOrSupervisor>
}
