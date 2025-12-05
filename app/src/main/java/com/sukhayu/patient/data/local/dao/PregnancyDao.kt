package com.sukhayu.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sukhayu.patient.data.local.entity.PregnancyEntity

/**
 * Data Access Object for pregnancy records.
 */
@Dao
interface PregnancyDao {

    /**
     * Insert or update a pregnancy record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPregnancy(entity: PregnancyEntity)

    /**
     * Get a pregnancy record by its ID.
     */
    @Query("SELECT * FROM pregnancies WHERE id = :id LIMIT 1")
    suspend fun getPregnancyById(id: String): PregnancyEntity?

    /**
     * Get all pregnancy records for a specific woman.
     */
    @Query("SELECT * FROM pregnancies WHERE womanId = :womanId ORDER BY createdAt DESC")
    suspend fun getPregnanciesForWoman(womanId: String): List<PregnancyEntity>

    /**
     * Get all unsynced pregnancy records for background sync.
     */
    @Query("SELECT * FROM pregnancies WHERE isSynced = 0")
    suspend fun getUnsyncedPregnancies(): List<PregnancyEntity>

    /**
     * Update the sync status of a pregnancy record.
     */
    @Query("UPDATE pregnancies SET isSynced = :synced, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateSyncStatus(
        id: String,
        synced: Boolean,
        timestamp: Long
    )

    /**
     * Delete a pregnancy record by ID.
     */
    @Query("DELETE FROM pregnancies WHERE id = :id")
    suspend fun deletePregnancyById(id: String)

    /**
     * Delete all pregnancy records (for testing).
     */
    @Query("DELETE FROM pregnancies")
    suspend fun deleteAll()
}
