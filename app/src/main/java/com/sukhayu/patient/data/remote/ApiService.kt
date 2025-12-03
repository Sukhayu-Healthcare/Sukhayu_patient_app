package com.sukhayu.patient.data.remote

import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.model.PatientRegistrationRequest
import com.sukhayu.patient.model.PatientRegistrationResponse
import retrofit2.Call
import retrofit2.http.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


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

data class GeneralSurveyRequest(
    val tableName: String,
    val date: String,
    val records: List<Map<String, Any>>
)

data class GeneralSurveyResponse(
    val message: String,
    val success: Boolean
)

data class GeneralScreeningsResponse(
    val message: String,
    val data: List<ScreeningData>
)

data class ScreeningData(
    val id: String,
    val patientId: String,
    val ashaId: String,
    val supervisorId: String,
    val tableName: String,
    val date: String,
    val time: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

data class AllPatientsResponse(
    val message: String?,
    val patients: List<PatientFromServer>
)

data class PatientFromServer(
    val patient_id: Int?,
    val gender: String?,
    val dob: String?,
    val phone: Long?,
    val profile_pic: String?,
    val village: String?,
    val taluka: String?,
    val district: String?,
    val supreme_id: Int?,
    val name: String?
)

fun PatientFromServer.toEntity(): PatientEntity {
    return PatientEntity(
        id = patient_id?.toString() ?: name ?: phone?.toString() ?: System.currentTimeMillis().toString(),
        name = name ?: "Unknown",
        phone = phone?.toString(),
        gender = gender,
        weightKg = null,
        supremeId = supreme_id?.toString(),
        age = dob?.let { calculateAgeFromDob(it) }
    )
}

private fun calculateAgeFromDob(dob: String): Int? {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birthDate = formatter.parse(dob) ?: return null
        val now = Calendar.getInstance()
        val dobCal = Calendar.getInstance().apply { time = birthDate }
        var age = now.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)
        if (now.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
            age -= 1
        }
        age
    } catch (e: Exception) {
        null
    }
}

data class PatientData(
    val id: String,
    val name: String,
    val phone: String,
    val village: String,
    val district: String,
    val taluka: String,
    val profilePic: String?
)

data class SupervisorSurveyDataResponse(
    val supervisor_id: String,
    val table: String,
    val date: String,
    val asha_count: Int,
    val count: Int,
    val records: List<Map<String, Any>>
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

    @DELETE("asha/supervisor/delete-asha/{id}")
    fun deleteAsha(
        @Header("Authorization") token: String,
        @Path("id") ashaId: String
    ): Call<Void>

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

    @GET("survey/supervisor/data/{tableName}/{date}")
    suspend fun getSupervisorSurveyData(
        @Header("Authorization") token: String,
        @Path("tableName") tableName: String,
        @Path("date") date: String
    ): SupervisorSurveyDataResponse
}
