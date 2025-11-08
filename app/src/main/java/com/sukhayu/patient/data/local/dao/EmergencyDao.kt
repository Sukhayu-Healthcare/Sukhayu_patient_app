// package com.sukhayu.patient.data.local.dao

// import androidx.room.*
// import com.sukhayu.patient.data.local.entities.EmergencyEntity

// @Dao
// interface EmergencyDao {

//     @Insert(onConflict = OnConflictStrategy.REPLACE)
//     suspend fun insert(event: EmergencyEntity)

//     @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC")
//     suspend fun getAllEvents(): List<EmergencyEntity>

//     @Query("DELETE FROM emergency_events")
//     suspend fun clearAll()
// }
