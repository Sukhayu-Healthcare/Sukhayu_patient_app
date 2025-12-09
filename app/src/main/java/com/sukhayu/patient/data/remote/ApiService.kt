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
import retrofit2.Response
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

/**
 * Must match backend JSON:
 * {
 *   "total": number,
 *   "page": number,
 *   "limit": number,
 *   "totalPages": number,
 *   "ashas": [ ... ]
 * }
 */
data class AshaListResponse(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
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

// ----------------------------
// New models for consultations
// ----------------------------

data class PrescriptionItem(
    val consultation_id: Int,
    val item_id: Int,
    val medicine_name: String?,
    val dosage: String?,
    val frequency: String?,
    val duration: String?,
    val instructions: String?
)

data class Consultation(
    val consultation_id: Int,
    val patient_id: Int,
    val doctor_id: Int?,
    val doctor_name: String?,
    val doctor_phone: String?,
    val diagnosis: String?,
    val notes: String?,
    val consultation_date: String,
    val items: List<PrescriptionItem>?
)

data class PatientConsultationsResponse(
    val consultations: List<Consultation>
)

data class CreateNoticeRequest(
    val title: String,
    val body: String,
    val target_village: String?,
    val target_district: String?,
    val target_taluka: String?
)

data class Notice(
    val notice_id: Int,
    val created_by: Int,
    val title: String,
    val body: String,
    val target_village: String?,
    val target_district: String?,
    val target_taluka: String?,
    val created_at: String
)

data class CreateNoticeResponse(
    val success: Boolean,
    val message: String,
    val notice: Notice
)

data class SaveFcmTokenRequest(
    val fcm_token: String
)

data class SaveFcmTokenResponse(
    val success: Boolean,
    val message: String,
    val token: Any?
)

data class SendToAshaRequest(
    val notice_id: Int
)

data class SendToAshaResponse(
    val success: Boolean,
    val message: String
)

<<<<<<< HEAD
data class PatientQueryRequest(
    val asha_id: Int? = null,
    val text: String,
    val voice_url: String? = null,
    val disease: String,
    val doc: String,
    val doc_id: Int? = null
)

data class QueryData(
    val query_id: Int,
    val patient_id: Int,
    val asha_id: Int?,
    val text: String,
    val voice_url: String?,
    val disease: String,
    val doc: String,
    val doc_id: Int?,
    val query_status: String,
    val done_or_not: Boolean
)

data class PatientQueryResponse(
    val message: String,
    val data: QueryData
)

data class PatientProfileResponse(
    val message: String,
    val patient: PatientProfileData,
    val familyProfiles: List<FamilyProfile>? = null,
    val ashaWorker: AshaWorkerProfile? = null
)

data class PatientProfileData(
    val patient_id: Int,
    val user_id: Int,
    val user_name: String?,
    val supreme_id: Int?,
    val gender: String?,
    val dob: String?,
    val phone: String?,
    val profile_pic: String?,
    val village: String?,
    val taluka: String?,
    val district: String?,
    val registered_asha_id: Int?,
    val created_at: String?
)

data class FamilyProfile(
    val patient_id: Int,
    val gender: String?,
    val dob: String?,
    val phone: String?,
    val profile_pic: String?,
    val village: String?,
    val taluka: String?,
    val district: String?
)

data class AshaWorkerProfile(
    val asha_id: Int,
    val village: String?,
    val taluka: String?,
    val district: String?,
    val profile_pic: String?,
    val user_id: Int,
    val asha_name: String?,
    val asha_phone: String?
)

=======
>>>>>>> 80dee916ea5736a372a33fdcdf61917785771827
interface ApiService {

    @POST("patient/v2/login")
    fun login(
        @Body body: LoginRequest
    ): Call<Map<String, Any>>

    @GET("asha/profile")
    fun getSupervisorProfile(
        @Header("Authorization") token: String
    ): Call<SupervisorProfile>
    // Fetches supervisor profile data from backend

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

    /**
     * UPDATED:
     * Adds `page` & `limit` as query params
     * Uses suspend for coroutine calls
     */
    @GET("asha/all-ashas")
<<<<<<< HEAD
    fun getAshaList(
        @Header("Authorization") token: String
    ): Call<AshaListResponse>
    // Fetches all ASHA workers list from backend
=======
    suspend fun getAshaList(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): AshaListResponse
>>>>>>> 80dee916ea5736a372a33fdcdf61917785771827

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
    // Fetches ASHA details from backend

    @GET("asha/patients/search")
    suspend fun searchPatients(
        @Header("Authorization") token: String,
        @Query("q") query: String
    ): PatientSearchResponse
    // Searches patients from backend

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
    // Fetches supervisor survey data from backend

    // Corrected endpoint: /supervisor/data/{table}/{date}
    @GET("survey/supervisor/data/{table}/{date}")
    suspend fun getSupervisorSurveyDataByTableAndDate(
        @Header("Authorization") authHeader: String,
        @Path("table") table: String,
        @Path("date") date: String
    ): SupervisorSurveyDataResponse
    // Fetches supervisor survey data by table and date from backend

    @POST("survey/genral")
    suspend fun submitGeneralSurvey(
        @Header("Authorization") authHeader: String,
        @Body body: GeneralSurveyRequest
    ): GeneralSurveyResponse

    @GET("survey/genral")
    suspend fun getGeneralScreenings(
        @Header("Authorization") authHeader: String
    ): GeneralScreeningsResponse
    // Fetches general screenings from backend

    @GET("patient/all")
    suspend fun getAllPatients(
        @Header("Authorization") authHeader: String
    ): AllPatientsResponse
    // Fetches all patients from backend

    @POST("survey/tb-first")
    suspend fun submitTbFirst(
        @Header("Authorization") authHeader: String,
        @Body body: TbFirstRequest
    ): TbFirstResponse

    @GET("survey/tb-first")
    suspend fun getTbFirstScreenings(
        @Header("Authorization") authHeader: String
    ): TbFirstScreeningsResponse
    // Fetches TB first screenings from backend

    @POST("survey/tb-followup")
    suspend fun submitTbFollowUp(
        @Header("Authorization") authHeader: String,
        @Body body: TbFollowUpRequest
    ): TbFollowUpResponse

    @GET("tb/followups/{tb_id}")
    suspend fun getTbFollowUps(
        @Header("Authorization") authHeader: String,
        @Path("tb_id") tbId: String
    ): TbFollowUpsResponse
    // Fetches TB follow-ups from backend

    @POST("survey/anc")
    suspend fun submitFirstAncVisit(
        @Header("Authorization") authHeader: String,
        @Body body: FirstAncVisitRequest
    ): FirstAncVisitResponse

    @GET("survey/anc")
    suspend fun getAncRecords(
        @Header("Authorization") authHeader: String
    ): AncRecordsResponse
    // Fetches ANC records from backend

    @POST("survey/anc-followup")
    suspend fun submitAncFollowUp(
        @Header("Authorization") authHeader: String,
        @Body body: AncFollowUpRequest
    ): AncFollowUpResponse

    // New endpoint to fetch patient's consultations (with items)
    @GET("patient/consultations")
    suspend fun getPatientConsultations(
        @Header("Authorization") authHeader: String
    ): PatientConsultationsResponse
    // Fetches patient consultations from backend

    @POST("notice/create-notice")
    fun createNotice(
        @Header("Authorization") token: String,
        @Body body: CreateNoticeRequest
    ): Call<CreateNoticeResponse>

    @POST("noti/save-token")
    fun saveFcmToken(
        @Header("Authorization") token: String,
        @Body body: SaveFcmTokenRequest
    ): Call<SaveFcmTokenResponse>

    @POST("asha/supervisor/send-to-asha")
    fun sendNoticeToAsha(
        @Header("Authorization") token: String,
        @Body body: SendToAshaRequest
    ): Call<SendToAshaResponse>
    // Sends notice to ASHA workers with notice_id

    @POST("analyze")
    suspend fun analyzeComplaint(
        @Body request: AnalyzeRequest
    ): Response<AnalyzeResponse>
<<<<<<< HEAD

    @POST("query/patient")
    fun submitPatientQuery(
        @Header("Authorization") token: String,
        @Body body: PatientQueryRequest
    ): Call<PatientQueryResponse>

    @GET("patient/profile")
    fun getPatientProfile(
        @Header("Authorization") token: String
    ): Call<PatientProfileResponse>
=======
>>>>>>> 80dee916ea5736a372a33fdcdf61917785771827
}
