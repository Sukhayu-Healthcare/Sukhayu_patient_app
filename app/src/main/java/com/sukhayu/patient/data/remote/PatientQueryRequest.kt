package com.sukhayu.patient.data.remote

data class PatientQueryRequest(
    // patient_id is extracted from JWT token on backend, not sent in body
    val asha_id: Int?,
    val text: String,
    val voice_url: String?,
    val disease: String,
    val doc: String,
    val doc_id: Int?,
    val query_status: String
)
