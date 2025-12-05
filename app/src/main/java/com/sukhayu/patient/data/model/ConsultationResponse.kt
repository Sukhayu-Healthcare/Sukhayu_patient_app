package com.sukhayu.patient.data.model

data class ConsultationResponse(
    val consultations: List<ConsultationWithItems>
)

data class ConsultationWithItems(
    val consultation_id: Int,
    val patient_id: Int,
    val doctor_id: Int,
    val diagnosis: String?,
    val notes: String?,
    val consultation_date: String,
    val doctor_name: String?,
    val doctor_phone: String?,
    val items: List<PrescriptionItem>
)

data class PrescriptionItem(
    val consultation_id: Int,
    val item_id: Int,
    val medicine_name: String,
    val dosage: String?,
    val frequency: String?,
    val duration: String?,
    val instructions: String?
)
