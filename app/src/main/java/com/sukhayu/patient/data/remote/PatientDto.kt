package com.sukhayu.patient.data.remote

import com.sukhayu.patient.data.local.entity.PatientEntity

data class PatientDto(
    val id: String,
    val patient_id: String,
    val name: String,
    val user_name: String,
    val phone: String,
    val village: String?,
    val district: String?,
    val gender: String?,
    val weight_kg: Double? = null,
    val supreme_id: String? = null,
    val age: Int? = null
)

// PatientSearchResponse moved to ApiService.kt to avoid redeclaration

fun PatientDto.toEntity(): PatientEntity {
    return PatientEntity(
        id = id,
        name = name,
        phone = phone,
        gender = gender,
        weightKg = weight_kg,
        supremeId = supreme_id,
        age = age
    )
}
