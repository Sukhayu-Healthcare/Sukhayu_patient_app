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

// --- Login & Response Models ---
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

// UserInfo and PatientInfo are defined in separate files (UserInfo.kt and PatientInfo.kt)
// to avoid "Redeclaration" errors.

// --- ASHA / Supervisor Models ---
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

// --- Consultation Models ---
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

// --- Notice / Drive / Notification Models ---
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

// --- Patient Profile Models ---
data class PatientProfileResponse(
    val message: String,
    val patient: PatientProfileData,
    val familyProfiles: List<FamilyProfile>? = null,
    val ashaWorker: AshaWorkerProfile? = null
)

data class PatientProfileData(
    val patient_id: Int,
    val user_id: Int,
    val user_name: String? = null,
    val name: String? = null,
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

// --- Patient Update Request ---
data class UpdatePatientProfileRequest(
    val name: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val village: String? = null,
    val taluka: String? = null,
    val district: String? = null,
    val profile_pic: String? = null
)

data class UpdatePatientProfileResponse(
    val message: String,
    val patient: PatientProfileData?
)

// --- Analyze / Search Models ---
data class PatientSearchResponse(
    val patients: List<PatientDto>
)

data class AnalyzeRequest(
    val complaint: String,
    val followup_answers: List<String> = emptyList()
)

data class AnalyzeResponse(
    val zone: String,
    val zone_label: String,
    val internal_disease: String,
    val patient_symptoms_line: String,
    val patient_action_line: String,
    val followup_question: String?,
    val followups_used: Int,
    val max_followups: Int,
    val baseline_disease: String,
    val baseline_zone: String
)

// --- Query Models ---
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
    val done_or_not: Boolean = false
)

data class PatientQueryResponse(
    val message: String,
    val data: QueryData
)

// --- Doctor & Appointment Models ---
data class Doctor(
    val doc_id: Int,
    val doc_name: String,
    val doc_profile_pic: String?,
    val doc_role: String?,
    val hospital_address: String?,
    val hospital_village: String?,
    val hospital_taluka: String?,
    val hospital_district: String?,
    val hospital_state: String?,
    val doc_phone: String?,
    val doc_speciality: String?,
    val doc_status: String?,
    val doc_created_at: String?
)

data class DoctorsListResponse(
    val total: Int,
    val doctors: List<Doctor>
)

data class BookAppointmentRequest(
    val doctor_id: Int,
    val appointment_date: String,
    val appointment_time: String,
    val notes: String? = null
)

data class Appointment(
    val appointment_id: Int,
    val patient_id: Int,
    val doctor_id: Int,
    val appointment_date: String,
    val appointment_time: String,
    val notes: String?
)

data class BookAppointmentResponse(
    val message: String,
    val appointment: List<Appointment>
)

data class PatientAppointmentsResponse(
    val total: Int,
    val appointments: List<Appointment>
)

// --- API Service Interface ---
interface ApiService {

    @POST("patient/v2/login")
    fun login(@Body body: LoginRequest): Call<Map<String, Any>>

    @GET("asha/profile")
    fun getSupervisorProfile(@Header("Authorization") token: String): Call<SupervisorProfile>

    @PUT("asha/profile")
    fun updateSupervisorProfile(@Header("Authorization") token: String, @Body body: SelfUpdateRequest): Call<UpdateProfileResponse>

    @POST("asha/register-asha")
    fun registerAsha(@Header("Authorization") token: String, @Body body: RegisterAshaRequest): Call<RegisterAshaResponse>

    @GET("asha/all-ashas")
    suspend fun getAshaList(@Header("Authorization") token: String, @Query("page") page: Int, @Query("limit") limit: Int): AshaListResponse

    @POST("asha/patient/register")
    fun registerPatient(@Header("Authorization") token: String, @Body body: PatientRegistrationRequest): Call<PatientRegistrationResponse>

    @GET("asha/details/{ashaId}")
    fun getAshaDetails(@Header("Authorization") token: String, @Path("ashaId") ashaId: String): Call<AshaDetailsResponse>

    @GET("asha/patients/search")
    suspend fun searchPatients(@Header("Authorization") token: String, @Query("q") query: String): PatientSearchResponse

    @PUT("asha/supervisor/update-asha/{id}")
    fun updateAsha(@Header("Authorization") token: String, @Path("id") ashaId: String, @Body body: UpdateAshaRequest): Call<UpdateAshaResponse>

    @GET("survey/supervisor/data/{table}/{date}")
    suspend fun getSupervisorSurveyDataByTableAndDate(@Header("Authorization") authHeader: String, @Path("table") table: String, @Path("date") date: String): SupervisorSurveyDataResponse

    @POST("survey/genral")
    suspend fun submitGeneralSurvey(@Header("Authorization") authHeader: String, @Body body: GeneralSurveyRequest): GeneralSurveyResponse

    @GET("survey/genral")
    suspend fun getGeneralScreenings(@Header("Authorization") authHeader: String): GeneralScreeningsResponse

    @GET("patient/all")
    suspend fun getAllPatients(@Header("Authorization") authHeader: String): AllPatientsResponse

    @POST("survey/tb-first")
    suspend fun submitTbFirst(@Header("Authorization") authHeader: String, @Body body: TbFirstRequest): TbFirstResponse

    @GET("survey/tb-first")
    suspend fun getTbFirstScreenings(@Header("Authorization") authHeader: String): TbFirstScreeningsResponse

    @POST("survey/tb-followup")
    suspend fun submitTbFollowUp(@Header("Authorization") authHeader: String, @Body body: TbFollowUpRequest): TbFollowUpResponse

    @GET("tb/followups/{tb_id}")
    suspend fun getTbFollowUps(@Header("Authorization") authHeader: String, @Path("tb_id") tbId: String): TbFollowUpsResponse

    @POST("survey/anc")
    suspend fun submitFirstAncVisit(@Header("Authorization") authHeader: String, @Body body: FirstAncVisitRequest): FirstAncVisitResponse

    @GET("survey/anc")
    suspend fun getAncRecords(@Header("Authorization") authHeader: String): AncRecordsResponse

    @POST("survey/anc-followup")
    suspend fun submitAncFollowUp(@Header("Authorization") authHeader: String, @Body body: AncFollowUpRequest): AncFollowUpResponse

    @GET("patient/consultations")
    suspend fun getPatientConsultations(@Header("Authorization") authHeader: String): PatientConsultationsResponse

    @POST("notice/create-notice")
    fun createNotice(@Header("Authorization") token: String, @Body body: CreateNoticeRequest): Call<CreateNoticeResponse>

    @POST("noti/save-token")
    fun saveFcmToken(@Header("Authorization") token: String, @Body body: SaveFcmTokenRequest): Call<SaveFcmTokenResponse>

    @POST("asha/supervisor/send-to-asha")
    fun sendNoticeToAsha(@Header("Authorization") token: String, @Body body: SendToAshaRequest): Call<SendToAshaResponse>

    @POST("analyze")
    suspend fun analyzeComplaint(@Body request: AnalyzeRequest): Response<AnalyzeResponse>

    @POST("query/patient")
    fun submitPatientQuery(@Header("Authorization") token: String, @Body body: PatientQueryRequest): Call<PatientQueryResponse>

    @GET("patient/profile")
    fun getPatientProfile(@Header("Authorization") token: String): Call<PatientProfileResponse>

    @PUT("patient/profile")
    fun updatePatientProfile(
        @Header("Authorization") token: String,
        @Body body: UpdatePatientProfileRequest
    ): Call<UpdatePatientProfileResponse>

    @GET("appointment")
    suspend fun getAvailableDoctors(@Header("Authorization") authHeader: String): DoctorsListResponse

    @POST("appointment")
    suspend fun bookAppointment(
        @Header("Authorization") authHeader: String,
        @Body body: BookAppointmentRequest
    ): BookAppointmentResponse

    @GET("appointment/my-appointments")
    suspend fun getPatientAppointments(@Header("Authorization") authHeader: String): PatientAppointmentsResponse
}
