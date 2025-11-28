package com.sukhayu.patient.model

data class PatientRegistrationResponse(
    val message: String,
    val user_id: Int? = null,
    val patient_id: Int? = null,
    val supreme_id: Int? = null
)

