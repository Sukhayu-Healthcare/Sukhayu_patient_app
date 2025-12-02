package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String?,
    val gender: String?,
    val weightKg: Double?,
    val supremeId: String?,
    val age: Int? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
