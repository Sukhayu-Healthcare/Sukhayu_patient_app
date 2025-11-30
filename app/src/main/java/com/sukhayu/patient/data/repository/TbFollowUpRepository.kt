package com.sukhayu.patient.data.repository

import com.sukhayu.patient.data.local.dao.TbFollowUpDao
import com.sukhayu.patient.data.local.entity.TbFollowUpEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for TB Treatment Follow-up (DOTS) data
 * Handles offline-first data persistence and future sync with NIKSHAY/backend
 */
class TbFollowUpRepository(
    private val tbFollowUpDao: TbFollowUpDao
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
}

