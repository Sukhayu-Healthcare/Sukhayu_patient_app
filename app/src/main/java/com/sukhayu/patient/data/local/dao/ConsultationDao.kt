package com.sukhayu.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sukhayu.patient.data.local.entity.ConsultationEntity

@Dao
interface ConsultationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(item: ConsultationEntity)

    @Query("SELECT * FROM consultations ORDER BY consultation_date DESC LIMIT 5")
    suspend fun getLatestFive(): List<ConsultationEntity>

    @Query("SELECT * FROM consultations ORDER BY consultation_date DESC")
    suspend fun getAll(): List<ConsultationEntity>
}
