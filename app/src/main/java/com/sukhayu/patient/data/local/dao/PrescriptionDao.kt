package com.sukhayu.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sukhayu.patient.data.local.entity.PrescriptionItemEntity

@Dao
interface PrescriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(item: PrescriptionItemEntity)

    @Query("SELECT * FROM prescription_items WHERE consultation_id = :consultationId")
    suspend fun getItemsForConsultation(consultationId: Int): List<PrescriptionItemEntity>
}
