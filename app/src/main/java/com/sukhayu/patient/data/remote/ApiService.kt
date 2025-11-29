package com.sukhayu.patient.data.remote

import com.sukhayu.patient.model.LoginResponse
import com.sukhayu.patient.model.PatientRegistrationRequest
import com.sukhayu.patient.model.PatientRegistrationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

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

data class RegisterAshaRequest(
    val name: String,
    val password: String,
    val phone: String,
    val village: String,
    val district: String,
    val taluka: String,
    val profilePic: String? = null,
    val supervisorId: String
)

data class AshaInfo(
    val userId: String,
    val ashaId: String,
    val supervisorId: String,
    val name: String,
    val phone: String,
    val role: String
)

data class RegisterAshaResponse(
    val message: String,
    val asha: AshaInfo
)

data class AshaWorker(
    val asha_id: String,
    val user_id: String,
    val name: String,
    val phone: String,
    val village: String,
    val district: String,
    val taluka: String,
    val profile_pic: String?,
    val role: String,
    val created_at: String
)

data class AshaListResponse(
    val message: String,
    val ashaWorkers: List<AshaWorker>
)

data class SupervisorProfile(
    val user_id: String,
    val user_name: String,
    val phone: String,
    val user_role: String,
    val asha_id: String,
    val village: String,
    val district: String,
    val taluka: String,
    val profile_pic: String?
)

data class AshaDetailsResponse(
    val user_id: String,
    val user_name: String,
    val phone: String,
    val user_role: String,
    val asha_id: String,
    val village: String,
    val district: String,
    val taluka: String,
    val profile_pic: String?,
    val supervisor_id: String?
)

interface ApiService {

    @POST("login")
    fun login(
        @Body body: LoginRequest
    ): Call<Map<String, Any>>

    @POST("asha/register-asha")
    fun registerAsha(
        @Header("Authorization") token: String,
        @Body body: RegisterAshaRequest
    ): Call<RegisterAshaResponse>

    @GET("asha/list/{supervisorId}")
    fun getAshaList(
        @Header("Authorization") token: String,
        @Path("supervisorId") supervisorId: String
    ): Call<AshaListResponse>

    @POST("asha/patient/register")
    fun registerPatient(
        @Header("Authorization") token: String,
        @Body body: PatientRegistrationRequest
    ): Call<PatientRegistrationResponse>

    @GET("asha/profile")
    fun getSupervisorProfile(
        @Header("Authorization") token: String
    ): Call<SupervisorProfile>

    @GET("asha/details/{ashaId}")
    fun getAshaDetails(
        @Header("Authorization") token: String,
        @Path("ashaId") ashaId: String
    ): Call<AshaDetailsResponse>

}
