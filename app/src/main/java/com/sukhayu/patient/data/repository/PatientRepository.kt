package com.sukhayu.patient.data.repository

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
     * Offline-first patient search:
     * 1. First search locally in Room
     * 2. Then try to fetch from API (if token available)
     * 3. Update local cache with API results
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

