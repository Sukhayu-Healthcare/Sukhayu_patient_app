package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val appointment_id: Int = 0,
    val patient_id: Int,
    val doctor_name: String,
    val doctor_phone: String,
    val doctor_type: String, // "community_health_officer" or "medical_officer"
    val appointment_date: String, // ISO format: yyyy-MM-dd
    val appointment_time: String?, // HH:mm format
    val notes: String?,
    val created_at: Long = System.currentTimeMillis(),
    val synced: Boolean = false, // Whether it's synced to centralized database
    val sync_status: String = "pending" // pending, synced, failed
)
