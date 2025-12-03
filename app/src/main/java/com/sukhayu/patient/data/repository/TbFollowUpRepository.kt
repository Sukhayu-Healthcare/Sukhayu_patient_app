package com.sukhayu.patient.data.repository

import com.sukhayu.patient.data.local.dao.TbFollowUpDao
import com.sukhayu.patient.data.local.entity.TbFollowUpEntity
import com.sukhayu.patient.data.remote.ApiService
import com.sukhayu.patient.data.remote.TbFollowUpRequest
import com.sukhayu.patient.data.remote.TbFollowUpResponse
import com.sukhayu.patient.data.remote.TbFollowUpsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for TB Treatment Follow-up (DOTS) data
 * Handles offline-first data persistence and sync with backend
 */
class TbFollowUpRepository(
    private val tbFollowUpDao: TbFollowUpDao,
    private val apiService: ApiService
) {

    /**
     * Save or update a TB follow-up record
     */
    suspend fun saveFollowUp(entity: TbFollowUpEntity) = withContext(Dispatchers.IO) {
        tbFollowUpDao.upsertTbFollowUp(entity)
    }

    /**
     * Get TB follow-up by ID
     */
    suspend fun getTbFollowUpById(id: String): TbFollowUpEntity? = withContext(Dispatchers.IO) {
        tbFollowUpDao.getTbFollowUpById(id)
    }

    /**
     * Get all TB follow-ups for a specific patient
     */
    suspend fun getFollowUpsForPatient(patientId: String): List<TbFollowUpEntity> =
        withContext(Dispatchers.IO) {
            tbFollowUpDao.getTbFollowUpsForPatient(patientId)
        }

    /**
     * Get latest follow-up for a patient
     */
    suspend fun getLatestFollowUpForPatient(patientId: String): TbFollowUpEntity? =
        withContext(Dispatchers.IO) {
            tbFollowUpDao.getLatestFollowUpForPatient(patientId)
        }

    /**
     * Get all unsynced TB follow-ups
     * TODO: Implement sync with NIKSHAY/backend API
     */
    suspend fun getUnsyncedFollowUps(): List<TbFollowUpEntity> = withContext(Dispatchers.IO) {
        tbFollowUpDao.getUnsyncedTbFollowUps()
    }

    /**
     * Mark TB follow-up as synced
     * TODO: Call this after successful sync with backend
     */
    suspend fun markAsSynced(id: String) = withContext(Dispatchers.IO) {
        tbFollowUpDao.updateSyncStatus(id, true, System.currentTimeMillis())
    }

    /**
     * Submit TB follow-up to backend (POST survey/tb-followup)
     * @param authToken Bearer token for authentication
     * @param request TB follow-up request data
     * @return TbFollowUpResponse containing message and followup_id
     */
    suspend fun submitTbFollowUpToBackend(
        authToken: String,
        request: TbFollowUpRequest
    ): TbFollowUpResponse = withContext(Dispatchers.IO) {
        apiService.submitTbFollowUp("Bearer $authToken", request)
    }

    /**
     * Get TB follow-ups from backend (GET tb/followups/{tb_id})
     * @param authToken Bearer token for authentication
     * @param tbId TB patient ID to fetch follow-ups for
     * @return TbFollowUpsResponse containing list of follow-ups
     */
    suspend fun getTbFollowUpsFromBackend(
        authToken: String,
        tbId: String
    ): TbFollowUpsResponse = withContext(Dispatchers.IO) {
        apiService.getTbFollowUps("Bearer $authToken", tbId)
    }
}
