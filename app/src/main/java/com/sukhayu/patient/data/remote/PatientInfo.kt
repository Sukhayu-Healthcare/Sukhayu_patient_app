package com.sukhayu.patient.data.remote

data class PatientInfo(
    val id: String,  // Added id property
    val patient_id: String,
    val name: String,  // Added name property
    val user_name: String,
    val phone: String,
    val gender: String?,
    val dob: String?,
    val village: String?,
    val district: String?,
    val taluka: String?,
    val weight_kg: Double? = null,  // Added weight_kg property
    val supreme_id: String? = null  // Added supreme_id property
)
