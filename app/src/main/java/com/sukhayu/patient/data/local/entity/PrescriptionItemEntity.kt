package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescription_items")
data class PrescriptionItemEntity(
    @PrimaryKey(autoGenerate = true) val item_id: Int = 0,
    val consultation_id: Int,
    val medicine_name: String,
    val dosage: String?,
    val frequency: String?,
    val duration: String?,
    val instructions: String?
)
