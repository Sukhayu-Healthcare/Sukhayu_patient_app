package com.sukhayu.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sukhayu.patient.data.local.entity.TbFollowUpEntity

@Dao
interface TbFollowUpDao {

    /**
     * Insert or update a TB follow-up record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTbFollowUp(entity: TbFollowUpEntity)

    /**
     * Get TB follow-up by ID
     */
    @Query("SELECT * FROM tb_follow_ups WHERE id = :id LIMIT 1")
    suspend fun getTbFollowUpById(id: String): TbFollowUpEntity?

    /**
     * Get all TB follow-ups for a specific patient (ordered by visit date descending)
     */
    @Query("SELECT * FROM tb_follow_ups WHERE patientId = :patientId ORDER BY visitDate DESC")
    suspend fun getTbFollowUpsForPatient(patientId: String): List<TbFollowUpEntity>

    /**
     * Get all unsynced TB follow-ups (for backend sync)
     */
    @Query("SELECT * FROM tb_follow_ups WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedTbFollowUps(): List<TbFollowUpEntity>

    /**
     * Mark TB follow-up as synced
     */
    @Query("UPDATE tb_follow_ups SET isSynced = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun markFollowUpAsSynced(id: String, timestamp: Long)

    /**
     * Get all TB follow-ups (for admin/reports)
     */
    @Query("SELECT * FROM tb_follow_ups ORDER BY createdAt DESC")
    suspend fun getAllTbFollowUps(): List<TbFollowUpEntity>

    /**
     * Get latest follow-up for a patient
     */
    @Query("SELECT * FROM tb_follow_ups WHERE patientId = :patientId ORDER BY visitDate DESC LIMIT 1")
    suspend fun getLatestFollowUpForPatient(patientId: String): TbFollowUpEntity?
}
