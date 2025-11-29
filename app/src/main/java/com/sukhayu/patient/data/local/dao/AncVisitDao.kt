package com.sukhayu.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sukhayu.patient.data.local.entity.AncVisitEntity

/**
 * Data Access Object for ANC (Antenatal Care) visit records.
 */
@Dao
interface AncVisitDao {

    /**
     * Insert or update an ANC visit record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVisit(entity: AncVisitEntity)

    /**
     * Get all ANC visits for a specific pregnancy, ordered by visit date (most recent first).
     */
    @Query("SELECT * FROM anc_visits WHERE pregnancyId = :pregnancyId ORDER BY visitDate DESC")
    suspend fun getVisitsForPregnancy(pregnancyId: String): List<AncVisitEntity>

    /**
     * Get all unsynced ANC visit records for background sync.
     */
    @Query("SELECT * FROM anc_visits WHERE isSynced = 0")
    suspend fun getUnsyncedVisits(): List<AncVisitEntity>

    /**
     * Update the sync status of an ANC visit record.
     */
    @Query("UPDATE anc_visits SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, synced: Boolean)

    /**
     * Delete an ANC visit record by ID.
     */
    @Query("DELETE FROM anc_visits WHERE id = :id")
    suspend fun deleteVisitById(id: String)
}

