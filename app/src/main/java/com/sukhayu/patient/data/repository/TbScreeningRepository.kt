package com.sukhayu.patient.data.repository

import com.sukhayu.patient.data.local.dao.TbScreeningDao
import com.sukhayu.patient.data.local.dao.SurveySummaryDao
import com.sukhayu.patient.data.local.entity.TbScreeningEntity
import com.sukhayu.patient.data.local.entity.fromTbScreening
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for TB Screening data
 * Handles offline-first data persistence and sync helpers
 */
class TbScreeningRepository(
    private val tbScreeningDao: TbScreeningDao,
    private val surveySummaryDao: SurveySummaryDao? = null
) {

    /**
     * Save or update a TB screening record.
     * Also creates/updates a corresponding SurveySummaryEntity for the View Surveys screen.
     *
     * @param entity The TB screening entity to save
     * @param ashaId The ASHA worker ID
     */
    suspend fun createOrUpdateTbScreening(entity: TbScreeningEntity, ashaId: String? = null) = withContext(Dispatchers.IO) {
        tbScreeningDao.upsertTbScreening(entity)

        // Also create a summary entry for the View Surveys screen
        ashaId?.let {
            surveySummaryDao?.let { dao ->
                val summary = fromTbScreening(entity, ashaId, isSynced = false)
                dao.insertOrUpdate(summary)
            }
        }
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
     * Mark TB screening as synced.
     * Also updates the corresponding SurveySummaryEntity row.
     *
     * @param id The TB screening entity ID
     * @param ashaId The ASHA worker ID (used to find and update the summary row)
     */
    suspend fun markAsSynced(id: String, ashaId: String? = null) = withContext(Dispatchers.IO) {
        tbScreeningDao.markTbScreeningAsSynced(id, System.currentTimeMillis())

        // Also mark the summary as synced
        ashaId?.let {
            surveySummaryDao?.let { dao ->
                dao.markSummaryAsSynced(id, ashaId)
            }
        }
    }
}
