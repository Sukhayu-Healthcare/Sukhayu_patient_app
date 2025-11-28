package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "consultations")
data class ConsultationEntity(
    @PrimaryKey(autoGenerate = true) val consultation_id: Int = 0,
    val patient_id: Int,
    val doctor_id: Int,
    val diagnosis: String?,
    val notes: String?,
    val consultation_date: Long
)
