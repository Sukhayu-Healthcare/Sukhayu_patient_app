package com.sukhayu.patient.data.local.dao

import androidx.room.*
import com.sukhayu.patient.data.local.entities.AshaWorkerEntity

@Dao
interface AshaWorkerDao {
    @Query("SELECT * FROM asha_workers ORDER BY asha_name ASC")
    suspend fun getAllAshaWorkers(): List<AshaWorkerEntity>

    @Query("SELECT * FROM asha_workers WHERE asha_id = :ashaId")
    suspend fun getAshaWorkerById(ashaId: String): AshaWorkerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ashaWorkers: List<AshaWorkerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ashaWorker: AshaWorkerEntity)

    @Query("DELETE FROM asha_workers")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM asha_workers")
    suspend fun getCount(): Int
}
