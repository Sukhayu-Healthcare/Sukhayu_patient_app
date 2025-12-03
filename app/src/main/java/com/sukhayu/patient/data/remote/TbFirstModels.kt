
package com.sukhayu.patient.data.remote

import com.google.gson.annotations.SerializedName

// ====== POST /tb-first ======

data class TbFirstRequest(
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("patient_name") val patientName: String,
    val age: Int?,
    val gender: String?,
    val mobile: String?,
    val address: String?,
    @SerializedName("screening_date") val screeningDate: String,

    @SerializedName("cough_2_weeks") val cough2Weeks: String?,
    @SerializedName("cough_blood") val coughBlood: String?,
    @SerializedName("fever_2_weeks") val fever2Weeks: String?,
    @SerializedName("night_sweats") val nightSweats: String?,
    @SerializedName("weight_loss") val weightLoss: String?,
    @SerializedName("chest_pain") val chestPain: String?,

    @SerializedName("household_tb") val householdTb: String?,
    @SerializedName("previous_tb") val previousTb: String?,
    @SerializedName("close_contact_tb") val closeContactTb: String?,
    @SerializedName("hiv_positive") val hivPositive: String?,
    val diabetes: String?,
    @SerializedName("tobacco_use") val tobaccoUse: String?,
    @SerializedName("alcohol_dependence") val alcoholDependence: String?,

    @SerializedName("sputum_collected") val sputumCollected: String?,
    @SerializedName("chest_xray") val chestXray: String?,
    @SerializedName("referred_to_higher_center") val referredToHigherCenter: String?
)

data class TbFirstResponse(
    val message: String,
    @SerializedName("tb_id") val tbId: Int
)

// ====== GET /tb-first ======

data class TbFirstScreeningDto(
    @SerializedName("patient_id") val patientId: String?,
    @SerializedName("patient_name") val patientName: String?,
    val age: Int?,
    val gender: String?,
    val mobile: String?,
    val address: String?,
    @SerializedName("asha_id") val ashaId: String?,
    @SerializedName("screening_date") val screeningDate: String?,

    @SerializedName("cough_2_weeks") val cough2Weeks: String?,
    @SerializedName("cough_blood") val coughBlood: String?,
    @SerializedName("fever_2_weeks") val fever2Weeks: String?,
    @SerializedName("night_sweats") val nightSweats: String?,
    @SerializedName("weight_loss") val weightLoss: String?,
    @SerializedName("chest_pain") val chestPain: String?,

    @SerializedName("household_tb") val householdTb: String?,
    @SerializedName("previous_tb") val previousTb: String?,
    @SerializedName("close_contact_tb") val closeContactTb: String?,
    @SerializedName("hiv_positive") val hivPositive: String?,
    val diabetes: String?,
    @SerializedName("tobacco_use") val tobaccoUse: String?,
    @SerializedName("alcohol_dependence") val alcoholDependence: String?,

    @SerializedName("sputum_collected") val sputumCollected: String?,
    @SerializedName("chest_xray") val chestXray: String?,
    @SerializedName("referred_to_higher_center") val referredToHigherCenter: String?
)

data class TbFirstScreeningsResponse(
    @SerializedName("tb_screenings") val tbScreening: TbFirstScreeningDto?
)
