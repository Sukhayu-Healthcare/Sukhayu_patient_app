package com.sukhayu.patient.data.remote

import com.google.gson.annotations.SerializedName

// ====== POST survey/tb-followup ======

data class TbFollowUpRequest(
    @SerializedName("tb_id") val tbId: String,
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("visit_date") val visitDate: String,
    @SerializedName("phase_of_treatment") val phaseOfTreatment: String,
    @SerializedName("visit_type") val visitType: String,
    @SerializedName("doses_missed") val dosesMissed: Int,
    val vomiting: Boolean,
    val jaundice: Boolean,
    @SerializedName("skin_rash") val skinRash: Boolean,
    @SerializedName("joint_pain") val jointPain: Boolean,
    @SerializedName("persistent_cough") val persistentCough: Boolean,
    val fever: Boolean,
    @SerializedName("weight_this_visit") val weightThisVisit: Double,
    @SerializedName("dot_provider") val dotProvider: String,
    @SerializedName("drug_box_checked") val drugBoxChecked: Boolean,
    @SerializedName("counselling_given") val counsellingGiven: Boolean,
    @SerializedName("treatment_continued") val treatmentContinued: Boolean,
    @SerializedName("referred_for_sideeffects") val referredForSideeffects: Boolean,
    @SerializedName("next_followup_date") val nextFollowupDate: String
)

data class TbFollowUpResponse(
    val message: String,
    @SerializedName("followup_id") val followupId: Int
)

// ====== GET tb/followups/{tb_id} ======

data class TbFollowUpDto(
    @SerializedName("tb_id") val tbId: String?,
    @SerializedName("patient_id") val patientId: String?,
    @SerializedName("visit_date") val visitDate: String?,
    @SerializedName("phase_of_treatment") val phaseOfTreatment: String?,
    @SerializedName("visit_type") val visitType: String?,
    @SerializedName("doses_missed") val dosesMissed: Int?,
    val vomiting: Boolean?,
    val jaundice: Boolean?,
    @SerializedName("skin_rash") val skinRash: Boolean?,
    @SerializedName("joint_pain") val jointPain: Boolean?,
    @SerializedName("persistent_cough") val persistentCough: Boolean?,
    val fever: Boolean?,
    @SerializedName("weight_this_visit") val weightThisVisit: Double?,
    @SerializedName("dot_provider") val dotProvider: String?,
    @SerializedName("drug_box_checked") val drugBoxChecked: Boolean?,
    @SerializedName("counselling_given") val counsellingGiven: Boolean?,
    @SerializedName("treatment_continued") val treatmentContinued: Boolean?,
    @SerializedName("referred_for_sideeffects") val referredForSideeffects: Boolean?,
    @SerializedName("next_followup_date") val nextFollowupDate: String?
)

data class TbFollowUpsResponse(
    @SerializedName("tb_followups") val tbFollowups: List<TbFollowUpDto>
)

