package com.sukhayu.patient.data.remote

import com.google.gson.annotations.SerializedName

data class GeneralSurveyRequest(
    @SerializedName("screening_date") val screeningDate: String,
    val village: String,
    val diabetes: String?,
    val hypertension: String?,
    @SerializedName("heart_disease") val heartDisease: String?,
    val stroke: String?,
    @SerializedName("kidney_problem") val kidneyProblem: String?,
    @SerializedName("other_condition") val otherCondition: String?,
    val urination: String?,
    val thirst: String?,
    @SerializedName("weight_loss") val weightLoss: String?,
    @SerializedName("blurred_vision") val blurredVision: String?,
    @SerializedName("chest_pain") val chestPain: String?,
    @SerializedName("shortness_of_breath") val shortnessOfBreath: String?,
    val weakness: String?,
    @SerializedName("family_history") val familyHistory: String?,
    @SerializedName("past_history") val pastHistory: String?,
    val tobacco: String?,
    val alcohol: String?,
    @SerializedName("physical_activity") val physicalActivity: String?,
    val diet: String?,
    @SerializedName("regular_health_check") val regularHealthCheck: String?,
    @SerializedName("current_medication") val currentMedication: String?,
    @SerializedName("medication_details") val medicationDetails: String?,
    @SerializedName("bp_check") val bpCheck: String?,
    @SerializedName("sugar_check") val sugarCheck: String?,
    val remarks: String?
)

data class GeneralSurveyResponse(
    val message: String,
    @SerializedName("screening_id") val screeningId: Int
)

data class GeneralScreeningDto(
    @SerializedName("screening_date") val screeningDate: String?,
    val village: String?,
    val diabetes: String?,
    val hypertension: String?,
    @SerializedName("heart_disease") val heartDisease: String?,
    val stroke: String?,
    @SerializedName("kidney_problem") val kidneyProblem: String?,
    @SerializedName("other_condition") val otherCondition: String?,
    val urination: String?,
    val thirst: String?,
    @SerializedName("weight_loss") val weightLoss: String?,
    @SerializedName("blurred_vision") val blurredVision: String?,
    @SerializedName("chest_pain") val chestPain: String?,
    @SerializedName("shortness_of_breath") val shortnessOfBreath: String?,
    val weakness: String?,
    @SerializedName("family_history") val familyHistory: String?,
    @SerializedName("past_history") val pastHistory: String?,
    val tobacco: String?,
    val alcohol: String?,
    @SerializedName("physical_activity") val physicalActivity: String?,
    val diet: String?,
    @SerializedName("regular_health_check") val regularHealthCheck: String?,
    @SerializedName("current_medication") val currentMedication: String?,
    @SerializedName("medication_details") val medicationDetails: String?,
    @SerializedName("bp_check") val bpCheck: String?,
    @SerializedName("sugar_check") val sugarCheck: String?,
    val remarks: String?,
    @SerializedName("asha_id") val ashaId: String?
)

data class GeneralScreeningsResponse(
    @SerializedName("screenings") val screenings: List<GeneralScreeningDto>?
)
