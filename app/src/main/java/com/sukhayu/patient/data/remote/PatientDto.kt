package com.sukhayu.patient.data.remote

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
    val supreme_id: String? = null
)

data class PatientSearchResponse(
    val patients: List<PatientDto>
)

