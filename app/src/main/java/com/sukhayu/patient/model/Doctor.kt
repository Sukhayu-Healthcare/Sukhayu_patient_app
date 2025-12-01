package com.sukhayu.patient.model

/**
 * Represents a doctor in the telemedicine system.
 */
data class Doctor(
    val id: String,
    val name: String,
    val specialty: String? = null,
    val rating: Float? = null,
    val experience: Int? = null,
    val available: Boolean = true
)

