package com.sukhayu.patient.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val patient: Patient?
)
