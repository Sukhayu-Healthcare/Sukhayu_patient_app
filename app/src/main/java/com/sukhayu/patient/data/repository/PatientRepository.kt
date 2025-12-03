package com.sukhayu.patient.data.repository

import com.sukhayu.patient.DummyData
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.data.remote.ApiService
import com.sukhayu.patient.data.remote.AllPatientsResponse
import com.sukhayu.patient.data.remote.GeneralScreeningsResponse
import com.sukhayu.patient.data.remote.GeneralSurveyRequest
import com.sukhayu.patient.data.remote.GeneralSurveyResponse
import com.sukhayu.patient.data.remote.PatientDto
import com.sukhayu.patient.data.remote.PatientFromServer
import com.sukhayu.patient.data.remote.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import retrofit2.HttpException
import com.sukhayu.patient.data.remote.TbFirstRequest
import com.sukhayu.patient.data.remote.TbFirstResponse
import com.sukhayu.patient.data.remote.TbFirstScreeningsResponse



class PatientRepository(
    private val db: AshaLocalDatabase,
    private val apiService: ApiService
) {

    /**
     * Initialize the local database with dummy patients if it's empty.
     * This ensures offline-first functionality for both Pregnancy/ANC and TB modules.
     *
     * Call this method once during app initialization (e.g., in Application class or main activity).
     */
    suspend fun initializeDummyDataIfNeeded() = withContext(Dispatchers.IO) {
        val count = db.patientDao().getPatientCount()
        if (count == 0) {
            // Database is empty - seed with dummy patients
            val dummyPatients = DummyData.getDummyPatients()
            db.patientDao().insertPatients(dummyPatients)
        }
    }

    /**
     * UNIFIED PATIENT SEARCH for all modules (Pregnancy/ANC + TB).
     *
     * Backend-first patient search with local DB fallback:
     * 1. Try to fetch from backend API first (if token available)
     * 2. If backend returns results, update local cache and return them
     * 3. If backend returns empty or fails, fallback to local DB search
     *
     * This single function should be used by:
     * - Pregnancy/ANC survey flows
     * - TB screening flows
     * - TB treatment follow-up flows
     */
    suspend fun searchPatients(query: String, token: String?): List<PatientEntity> =
        withContext(Dispatchers.IO) {
            // Step 1: Try backend first if token is available
            if (token != null) {
                try {
                    val response = apiService.searchPatients("Bearer $token", query)
                    val remotePatients = response.patients.map { it.toEntity() }

                    // Step 2: If backend returns results, update local cache and return them
                    if (remotePatients.isNotEmpty()) {
                        db.patientDao().insertPatients(remotePatients)
                        return@withContext remotePatients
                    }
                    // If backend returns empty list, fall through to local search
                } catch (e: Exception) {
                    // If API fails, fall through to local search
                    Log.e("PatientRepository", "Backend search failed, using local DB fallback", e)
                }
            }

            // Step 3: Fallback to local DB search
            return@withContext db.patientDao().searchPatients(query)
        }

    suspend fun getPatientById(patientId: String): PatientEntity? =
        withContext(Dispatchers.IO) {
            db.patientDao().getPatientById(patientId)
        }

    suspend fun submitGeneralSurvey(
        token: String,
        request: GeneralSurveyRequest
    ): GeneralSurveyResponse =
        withContext(Dispatchers.IO) {
            apiService.submitGeneralSurvey("Bearer $token", request)
        }

    suspend fun getGeneralScreenings(token: String): GeneralScreeningsResponse =
        withContext(Dispatchers.IO) {
            apiService.getGeneralScreenings("Bearer $token")
        }

    suspend fun submitTbFirst(token: String, request: TbFirstRequest): TbFirstResponse =
        withContext(Dispatchers.IO) {
            apiService.submitTbFirst("Bearer $token", request)
        }

    suspend fun getTbFirstScreenings(token: String): TbFirstScreeningsResponse =
        withContext(Dispatchers.IO) {
            apiService.getTbFirstScreenings("Bearer $token")
        }


    suspend fun syncPatientsFromServer(token: String) = withContext(Dispatchers.IO) {
        try {
            Log.d("PatientSync", "Calling /patient/all with token (trimmed)=${token.take(15)}...")

            val response = apiService.getAllPatients("Bearer $token")

            val count = response.patients?.size ?: 0
            Log.d("PatientSync", "Got /patient/all response, patients count = $count")

            if (response.patients.isNullOrEmpty()) {
                Log.w("PatientSync", "No patients received from server, skipping insert")
                return@withContext
            }

            val dao = db.patientDao()
            response.patients.forEach { serverPatient ->
                try {
                    val entity = serverPatient.toEntity()
                    Log.d("PatientSync", "Inserting patient id=${entity.id}, name=${entity.name}")
                    dao.insertOrUpdate(entity)
                } catch (e: Exception) {
                    Log.e("PatientSync", "Failed to insert patient ${serverPatient.patient_id}", e)
                }
            }

            Log.d("PatientSync", "Finished inserting patients into local DB")
        } catch (e: HttpException) {
            Log.e("PatientSync", "HTTP error while syncing patients: code=${e.code()}", e)
        } catch (e: Exception) {
            Log.e("PatientSync", "Unexpected error while syncing patients", e)
        }
    }
}
