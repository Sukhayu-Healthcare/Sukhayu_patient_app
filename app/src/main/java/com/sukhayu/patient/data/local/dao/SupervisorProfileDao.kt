package com.sukhayu.patient.data.local.dao

import androidx.room.*
import com.sukhayu.patient.data.local.entities.SupervisorProfileEntity

@Dao
interface SupervisorProfileDao {
    @Query("SELECT * FROM supervisor_profile LIMIT 1")
    suspend fun getProfile(): SupervisorProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: SupervisorProfileEntity)

    @Query("DELETE FROM supervisor_profile")
    suspend fun deleteAll()
}
