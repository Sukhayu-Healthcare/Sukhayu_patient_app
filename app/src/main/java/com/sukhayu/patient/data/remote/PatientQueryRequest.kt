package com.sukhayu.patient.data.remote

data class PatientQueryRequest(
    val patient_id: Int,
    val asha_id: Int?,
    val text: String,
    val voice_url: String?,
    val disease: String,
    val doc: String,
    val doc_id: Int?,
    val query_status: String
)
