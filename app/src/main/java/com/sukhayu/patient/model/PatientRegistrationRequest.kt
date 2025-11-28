package com.sukhayu.patient.model

data class PatientRegistrationRequest(
    val name: String,
    val password: String,
    val gender: String,
    val dob: String,  // ISO format: yyyy-MM-dd
    val phone: String,
    val profile_pic: String? = null,  // Base64 string or null
    val village: String,
    val taluka: String,
    val district: String,
    val history: List<HealthHistoryItem> = emptyList(),
    val supreme_id: Int? = null
)

