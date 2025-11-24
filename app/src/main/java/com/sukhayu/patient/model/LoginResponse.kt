package com.sukhayu.patient.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,           // Backend returns this
    val patient: Patient?         // If backend sends patient data
)
