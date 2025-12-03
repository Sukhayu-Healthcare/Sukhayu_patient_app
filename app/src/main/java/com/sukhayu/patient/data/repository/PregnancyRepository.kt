package com.sukhayu.patient.data.repository

import android.util.Log
import com.sukhayu.patient.asha.ui.surveys.tb.ResultState
import com.sukhayu.patient.data.local.dao.PregnancyDao
import com.sukhayu.patient.data.local.entity.PregnancyEntity
import com.sukhayu.patient.data.remote.AncRecordsResponse
import com.sukhayu.patient.data.remote.ApiService
import com.sukhayu.patient.data.remote.FirstAncVisitRequest
import com.sukhayu.patient.data.remote.FirstAncVisitResponse
import com.sukhayu.patient.data.remote.AncFollowUpRequest
import com.sukhayu.patient.data.remote.AncFollowUpResponse
import com.sukhayu.patient.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Repository for managing pregnancy records with offline-first approach.
 */
class PregnancyRepository(
    private val pregnancyDao: PregnancyDao,
    private val apiService: ApiService
) {

    /**
     * Create or update a pregnancy record.
     * Sets timestamps and marks as unsynced for later background sync.
     */
    suspend fun createOrUpdatePregnancy(entity: PregnancyEntity) = withContext(Dispatchers.IO) {
        val updatedEntity = entity.copy(
            updatedAt = System.currentTimeMillis(),
            isSynced = false // Mark as unsynced for background sync
        )
        pregnancyDao.upsertPregnancy(updatedEntity)
        // TODO: enqueue for sync (e.g., insert into SyncQueue with entity id and type)
    }

    /**
     * Get a pregnancy record by its ID.
     */
    suspend fun getPregnancyById(id: String): PregnancyEntity? = withContext(Dispatchers.IO) {
        pregnancyDao.getPregnancyById(id)
    }

    /**
     * Get all pregnancy records for a specific woman.
     */
    suspend fun getPregnanciesForWoman(womanId: String): List<PregnancyEntity> =
        withContext(Dispatchers.IO) {
            pregnancyDao.getPregnanciesForWoman(womanId)
        }

    /**
     * Get all unsynced pregnancy records for background sync.
     */
    suspend fun getUnsyncedPregnancies(): List<PregnancyEntity> = withContext(Dispatchers.IO) {
        pregnancyDao.getUnsyncedPregnancies()
    }

    /**
     * Mark a pregnancy record as synced after successful API upload.
     */
    suspend fun markAsSynced(id: String) = withContext(Dispatchers.IO) {
        pregnancyDao.updateSyncStatus(id, true)
    }

    /**
     * Delete a pregnancy record by ID.
     */
    suspend fun deletePregnancyById(id: String) = withContext(Dispatchers.IO) {
        pregnancyDao.deletePregnancyById(id)
    }

    /**
     * Submit First ANC Visit to backend (POST survey/anc)
     * @param request First ANC visit request data
     * @return ResultState wrapper with response or error
     */
    suspend fun submitFirstAncVisit(request: FirstAncVisitRequest): ResultState<FirstAncVisitResponse> {
        return withContext(Dispatchers.IO) {
            val token = TokenManager.getToken()
            if (token.isBlank()) {
                return@withContext ResultState.Error("Authentication token missing")
            }

            try {
                val response = apiService.submitFirstAncVisit("Bearer $token", request)
                ResultState.Success(response)
            } catch (e: HttpException) {
                val body = e.response()?.errorBody()?.string()
                Log.e("ANC_REPO", "HTTP ${e.code()} while submitting first ANC visit. Body: $body", e)
                ResultState.Error("Server error: ${e.code()}")
            } catch (e: Exception) {
                Log.e("ANC_REPO", "Error submitting first ANC visit", e)
                ResultState.Error(e.message ?: "Unable to submit first ANC visit")
            }
        }
    }

    /**
     * Get ANC records from backend (GET survey/anc)
     * @return ResultState wrapper with records or error
     */
    suspend fun getAncRecords(): ResultState<AncRecordsResponse> {
        return withContext(Dispatchers.IO) {
            val token = TokenManager.getToken()
            if (token.isBlank()) {
                return@withContext ResultState.Error("Authentication token missing")
            }

            try {
                val response = apiService.getAncRecords("Bearer $token")
                ResultState.Success(response)
            } catch (e: HttpException) {
                val body = e.response()?.errorBody()?.string()
                Log.e("ANC_REPO", "HTTP ${e.code()} while fetching ANC records. Body: $body", e)
                ResultState.Error("Server error: ${e.code()}")
            } catch (e: Exception) {
                Log.e("ANC_REPO", "Error fetching ANC records", e)
                ResultState.Error(e.message ?: "Unable to fetch ANC records")
            }
        }
    }

    /**
     * Submit ANC Follow-Up visit to backend (POST survey/anc-followup)
     * @param request ANC follow-up request data
     * @return ResultState wrapper with response or error
     */
    suspend fun submitAncFollowUp(request: AncFollowUpRequest): ResultState<AncFollowUpResponse> {
        return withContext(Dispatchers.IO) {
            val token = TokenManager.getToken()
            if (token.isBlank()) {
                return@withContext ResultState.Error("Authentication token missing")
            }

            try {
                val response = apiService.submitAncFollowUp("Bearer $token", request)
                ResultState.Success(response)
            } catch (e: HttpException) {
                val body = e.response()?.errorBody()?.string()
                Log.e("ANC_REPO", "HTTP ${e.code()} while submitting ANC follow-up. Body: $body", e)
                ResultState.Error("Server error: ${e.code()}")
            } catch (e: Exception) {
                Log.e("ANC_REPO", "Error submitting ANC follow-up", e)
                ResultState.Error(e.message ?: "Unable to submit ANC follow-up")
            }
        }
    }
}
