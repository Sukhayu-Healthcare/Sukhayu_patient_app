package com.sukhayu.patient.data.remote

import com.sukhayu.patient.model.PatientRegistrationRequest
import com.sukhayu.patient.model.PatientRegistrationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query
import com.sukhayu.patient.data.remote.SupervisorSurveyDataResponse


data class LoginRequest(
    val phone: String,
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
    val asha_name: String,
    val asha_phone: String,
    val village: String,
    val district: String,
    val taluka: String,
    val profile_pic: String?
)

data class AshaListResponse(
    val ashas: List<AshaWorker>
)

data class SupervisorProfile(
    val user_id: String?,
    val user_name: String?,
    val phone: String?,
    val user_role: String?,
    val date_of_birth: String?,
    val user_created_at: String?,
    val asha_id: String?,
    val village: String?,
    val district: String?,
    val taluka: String?,
    val profile_pic: String?,
    val supervisor_id: String?
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

data class UpdateProfileResponse(
    val message: String,
    val profile: SupervisorProfile
)

data class UpdateAshaResponse(
    val message: String,
    val updatedAsha: AshaWorker
)

data class UpdateAshaRequest(
    val asha_name: String?,
    val asha_village: String?,
    val asha_district: String?,
    val asha_taluka: String?,
    val supervisor_id: String?
)

data class SelfUpdateRequest(
    val asha_password: String? = null,
    val asha_phone: String? = null,
    val asha_profile_pic: String? = null
)

interface ApiService {

    @POST("patient/v2/login")
    fun login(
        @Body body: LoginRequest
    ): Call<Map<String, Any>>

    @GET("asha/profile")
    fun getSupervisorProfile(
        @Header("Authorization") token: String
    ): Call<SupervisorProfile>

    @PUT("asha/profile")
    fun updateSupervisorProfile(
        @Header("Authorization") token: String,
        @Body body: SelfUpdateRequest
    ): Call<UpdateProfileResponse>


    @POST("asha/register-asha")
    fun registerAsha(
        @Header("Authorization") token: String,
        @Body body: RegisterAshaRequest
    ): Call<RegisterAshaResponse>

    @GET("asha/all-ashas")
    fun getAshaList(
        @Header("Authorization") token: String
    ): Call<AshaListResponse>

    @POST("asha/patient/register")
    fun registerPatient(
        @Header("Authorization") token: String,
        @Body body: PatientRegistrationRequest
    ): Call<PatientRegistrationResponse>

    @GET("asha/details/{ashaId}")
    fun getAshaDetails(
        @Header("Authorization") token: String,
        @Path("ashaId") ashaId: String
    ): Call<AshaDetailsResponse>

    @GET("asha/patients/search")
    suspend fun searchPatients(
        @Header("Authorization") token: String,
        @Query("q") query: String
    ): PatientSearchResponse

    @PUT("asha/supervisor/update-asha/{id}")
    fun updateAsha(
        @Header("Authorization") token: String,
        @Path("id") ashaId: String,
        @Body body: UpdateAshaRequest
    ): Call<UpdateAshaResponse>

    // Supervisor: fetch survey data for a given table + date
    @GET("supervisor/surveys")
    suspend fun getSupervisorSurveyData(
        @Header("Authorization") authHeader: String,
        @Query("table") table: String,
        @Query("date") date: String
    ): SupervisorSurveyDataResponse



    @POST("survey/genral")
    suspend fun submitGeneralSurvey(
        @Header("Authorization") authHeader: String,
        @Body body: GeneralSurveyRequest
    ): GeneralSurveyResponse

    @GET("survey/genral")
    suspend fun getGeneralScreenings(
        @Header("Authorization") authHeader: String
    ): GeneralScreeningsResponse

    @GET("patient/all")
    suspend fun getAllPatients(
        @Header("Authorization") authHeader: String
    ): AllPatientsResponse

    @POST("survey/tb-first")
    suspend fun submitTbFirst(
        @Header("Authorization") authHeader: String,
        @Body body: TbFirstRequest
    ): TbFirstResponse

    @GET("survey/tb-first")
    suspend fun getTbFirstScreenings(
        @Header("Authorization") authHeader: String
    ): TbFirstScreeningsResponse


}
