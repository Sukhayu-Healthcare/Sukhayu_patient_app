package com.sukhayu.patient.data.remote

import com.google.gson.annotations.SerializedName

data class RegisterAshaRequest(
    val name: String,
    val password: String,
    val phone: String,
    val village: String,
    val district: String,
    val taluka: String,
    val profilePic: String? = null
)

data class AnalyzeRequest(
    val complaint: String,
    val followup_answers: List<String> = emptyList()
)

data class AnalyzeResponse(
    val zone: String,
    val zone_label: String,
    val internal_disease: String,
    val patient_symptoms_line: String,
    val patient_action_line: String,
    val followup_question: String?, // optional
    val followups_used: Int,
    val max_followups: Int,
    val baseline_disease: String,
    val baseline_zone: String
)