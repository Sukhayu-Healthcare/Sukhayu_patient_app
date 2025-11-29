package com.sukhayu.patient.model

/**
 * Represents a doctor in the telemedicine system.
 */
data class Doctor(
    val id: String,
    val name: String,
    val specialty: String,
    val rating: Double,
    val experience: Int? = null,
    val availability: String? = null,
    val consultationFee: Double? = null,
    val imageUrl: String? = null
)

