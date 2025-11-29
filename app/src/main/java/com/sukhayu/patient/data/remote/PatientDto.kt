package com.sukhayu.patient.data.remote

data class PatientDto(
    val id: String,
    val name: String,
    val phone: String?,
    val gender: String?,
    val weight_kg: Float?,
    val supreme_id: String?
)

data class PatientSearchResponse(
    val patients: List<PatientDto>
)

