package com.sukhayu.patient.data.repository

import com.sukhayu.patient.data.local.dao.PregnancyDao
import com.sukhayu.patient.data.local.entity.PregnancyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing pregnancy records with offline-first approach.
 */
class PregnancyRepository(
    private val pregnancyDao: PregnancyDao
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
}

