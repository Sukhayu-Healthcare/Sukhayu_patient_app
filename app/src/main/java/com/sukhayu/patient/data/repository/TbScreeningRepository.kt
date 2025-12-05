package com.sukhayu.patient.data.repository

import com.sukhayu.patient.data.local.dao.TbScreeningDao
import com.sukhayu.patient.data.local.entity.TbScreeningEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for TB Screening data
 * Handles offline-first data persistence and sync helpers
 */
class TbScreeningRepository(
    private val tbScreeningDao: TbScreeningDao
) {

    /**
     * Save or update a TB screening record
     */
    suspend fun createOrUpdateTbScreening(entity: TbScreeningEntity) = withContext(Dispatchers.IO) {
        tbScreeningDao.upsertTbScreening(entity)
    }

    /**
     * Get TB screening by ID
     */
    suspend fun getTbScreeningById(id: String): TbScreeningEntity? = withContext(Dispatchers.IO) {
        tbScreeningDao.getTbScreeningById(id)
    }

    /**
     * Get all TB screenings for a specific patient
     */
    suspend fun getTbScreeningsForPatient(patientId: String): List<TbScreeningEntity> =
        withContext(Dispatchers.IO) {
            tbScreeningDao.getTbScreeningsForPatient(patientId)
        }

    /**
     * Get all unsynced TB screenings
     */
    suspend fun getUnsyncedTbScreenings(): List<TbScreeningEntity> = withContext(Dispatchers.IO) {
        tbScreeningDao.getUnsyncedTbScreenings()
    }

    /**
     * Mark TB screening as synced
     */
    suspend fun markAsSynced(id: String) = withContext(Dispatchers.IO) {
        tbScreeningDao.markTbScreeningAsSynced(id, System.currentTimeMillis())
    }
}
