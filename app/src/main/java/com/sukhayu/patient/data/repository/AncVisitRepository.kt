package com.sukhayu.patient.data.repository

import com.sukhayu.patient.data.local.dao.AncVisitDao
import com.sukhayu.patient.data.local.entity.AncVisitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing ANC visit records with offline-first approach.
 */
class AncVisitRepository(
    private val ancVisitDao: AncVisitDao
) {

    /**
     * Add or update an ANC visit record.
     * Sets timestamps and marks as unsynced for later background sync.
     */
    suspend fun addOrUpdateVisit(visit: AncVisitEntity) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val updatedEntity = visit.copy(
            createdAt = if (visit.createdAt == 0L) now else visit.createdAt,
            updatedAt = now,
            isSynced = false // Mark as unsynced for background sync
        )
        ancVisitDao.upsertVisit(updatedEntity)
        // TODO: enqueue for sync (e.g., mark as unsynced or add to sync queue)
    }

    /**
     * Get all ANC visits for a specific pregnancy, ordered by visit date (most recent first).
     */
    suspend fun getVisitsForPregnancy(pregnancyId: String): List<AncVisitEntity> =
        withContext(Dispatchers.IO) {
            ancVisitDao.getVisitsForPregnancy(pregnancyId)
        }

    /**
     * Get all unsynced ANC visit records for background sync.
     */
    suspend fun getUnsyncedVisits(): List<AncVisitEntity> = withContext(Dispatchers.IO) {
        ancVisitDao.getUnsyncedVisits()
    }

    /**
     * Mark an ANC visit record as synced after successful API upload.
     */
    suspend fun markVisitSynced(id: String) = withContext(Dispatchers.IO) {
        ancVisitDao.updateSyncStatus(id, true)
    }

    /**
     * Delete an ANC visit record by ID.
     */
    suspend fun deleteVisitById(id: String) = withContext(Dispatchers.IO) {
        ancVisitDao.deleteVisitById(id)
    }
}

