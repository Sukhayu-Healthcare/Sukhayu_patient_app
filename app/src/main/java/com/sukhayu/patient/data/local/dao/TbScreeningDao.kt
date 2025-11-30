package com.sukhayu.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sukhayu.patient.data.local.entity.TbScreeningEntity

@Dao
interface TbScreeningDao {

    /**
     * Insert or update a TB screening record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTbScreening(entity: TbScreeningEntity)

    /**
     * Get TB screening by ID
     */
    @Query("SELECT * FROM tb_screenings WHERE id = :id LIMIT 1")
    suspend fun getTbScreeningById(id: String): TbScreeningEntity?

    /**
     * Get all TB screenings for a specific patient
     */
    @Query("SELECT * FROM tb_screenings WHERE patientId = :patientId ORDER BY dateOfScreening DESC")
    suspend fun getTbScreeningsForPatient(patientId: String): List<TbScreeningEntity>

    /**
     * Get all unsynced TB screenings (for future backend sync)
     */
    @Query("SELECT * FROM tb_screenings WHERE isSynced = 0")
    suspend fun getUnsyncedTbScreenings(): List<TbScreeningEntity>

    /**
     * Update sync status
     */
    @Query("UPDATE tb_screenings SET isSynced = :synced, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateSyncStatus(id: String, synced: Boolean, timestamp: Long)

    /**
     * Get all TB screenings (for admin/reports)
     */
    @Query("SELECT * FROM tb_screenings ORDER BY createdAt DESC")
    suspend fun getAllTbScreenings(): List<TbScreeningEntity>
}

