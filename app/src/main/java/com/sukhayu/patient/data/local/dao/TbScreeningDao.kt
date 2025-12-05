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
    @Query("SELECT * FROM tb_screenings WHERE patientId = :patientId ORDER BY createdAt DESC")
    suspend fun getTbScreeningsForPatient(patientId: String): List<TbScreeningEntity>

    /**
     * Get all unsynced TB screenings (for backend sync)
     */
    @Query("SELECT * FROM tb_screenings WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedTbScreenings(): List<TbScreeningEntity>

    /**
     * Mark TB screening as synced
     */
    @Query("UPDATE tb_screenings SET isSynced = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun markTbScreeningAsSynced(id: String, timestamp: Long)

    /**
     * Get all TB screenings (for debug/reports)
     */
    @Query("SELECT * FROM tb_screenings ORDER BY createdAt DESC")
    suspend fun getAllTbScreenings(): List<TbScreeningEntity>
}
