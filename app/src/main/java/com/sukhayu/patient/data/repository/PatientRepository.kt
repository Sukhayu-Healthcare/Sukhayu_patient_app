package com.sukhayu.patient.data.repository

import com.sukhayu.patient.DummyData
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.data.remote.ApiService
import com.sukhayu.patient.data.remote.PatientDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
     * Offline-first patient search:
     * 1. First search locally in Room
     * 2. Then try to fetch from API (if token available)
     * 3. Update local cache with API results
     *
     * This single function should be used by:
     * - Pregnancy/ANC survey flows
     * - TB screening flows
     * - TB treatment follow-up flows
     */
    suspend fun searchPatients(query: String, token: String?): List<PatientEntity> =
        withContext(Dispatchers.IO) {
            val searchPattern = "%$query%"

            // Step 1: Search locally
            val localResults = db.patientDao().searchPatients(searchPattern)

            // Step 2: Try to fetch from API if token is available
            if (token != null) {
                try {
                    val response = apiService.searchPatients("Bearer $token", query)
                    val remotePatients = response.patients.map { it.toEntity() }

                    // Step 3: Update local cache
                    if (remotePatients.isNotEmpty()) {
                        db.patientDao().insertPatients(remotePatients)
                    }

                    // Return remote results if available
                    return@withContext remotePatients
                } catch (e: Exception) {
                    // If API fails, return local results
                    e.printStackTrace()
                }
            }

            // Return local results if API failed or no token
            return@withContext localResults
        }

    suspend fun getPatientById(patientId: String): PatientEntity? =
        withContext(Dispatchers.IO) {
            db.patientDao().getPatientById(patientId)
        }

    private fun PatientDto.toEntity(): PatientEntity {
        return PatientEntity(
            id = this.id,
            name = this.name,
            phone = this.phone,
            gender = this.gender,
            weightKg = this.weight_kg,
            supremeId = this.supreme_id
        )
    }
}

