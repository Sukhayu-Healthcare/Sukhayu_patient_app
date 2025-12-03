package com.sukhayu.patient.data.remote

import com.google.gson.annotations.SerializedName

// ====== POST survey/anc ======

data class FirstAncVisitRequest(
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("first_anc_visit_date") val firstAncVisitDate: String,
    @SerializedName("lmp_date") val lmpDate: String?,
    val edd: String?,
    val gravida: Int?,
    val para: Int?,
    @SerializedName("living_children") val livingChildren: Int?,
    @SerializedName("previous_serious_complication") val previousSeriousComplication: Boolean,
    @SerializedName("severe_bleeding_now") val severeBleedingNow: Boolean,
    val convulsions: Boolean,
    @SerializedName("high_bp_earlier") val highBpEarlier: Boolean,
    @SerializedName("illness_diabetes") val illnessDiabetes: Boolean,
    @SerializedName("illness_high_bp") val illnessHighBp: Boolean,
    @SerializedName("illness_heart_disease") val illnessHeartDisease: Boolean,
    @SerializedName("illness_tb") val illnessTb: Boolean,
    @SerializedName("illness_hiv") val illnessHiv: Boolean,
    @SerializedName("illness_other") val illnessOther: String?,
    @SerializedName("place_of_anc_care") val placeOfAncCare: String?,
    @SerializedName("planned_place_delivery") val plannedPlaceDelivery: String?,
    @SerializedName("danger_signs_explained") val dangerSignsExplained: Boolean,
    @SerializedName("next_visit_date") val nextVisitDate: String?
)

data class FirstAncVisitResponse(
    val message: String,
    @SerializedName("anc_id") val ancId: Long
)

// ====== GET survey/anc ======

data class AncRecordDto(
    @SerializedName("anc_id") val ancId: Long?,
    @SerializedName("pregnant_woman_id") val pregnantWomanId: String?,
    @SerializedName("first_anc_visit_date") val firstAncVisitDate: String?,
    @SerializedName("lmp_date") val lmpDate: String?,
    val edd: String?,
    val gravida: Int?,
    val para: Int?,
    @SerializedName("living_children") val livingChildren: Int?,
    @SerializedName("previous_serious_complication") val previousSeriousComplication: Boolean?,
    @SerializedName("severe_bleeding_now") val severeBleedingNow: Boolean?,
    val convulsions: Boolean?,
    @SerializedName("high_bp_earlier") val highBpEarlier: Boolean?,
    @SerializedName("illness_diabetes") val illnessDiabetes: Boolean?,
    @SerializedName("illness_high_bp") val illnessHighBp: Boolean?,
    @SerializedName("illness_heart_disease") val illnessHeartDisease: Boolean?,
    @SerializedName("illness_tb") val illnessTb: Boolean?,
    @SerializedName("illness_hiv") val illnessHiv: Boolean?,
    @SerializedName("illness_other") val illnessOther: String?,
    @SerializedName("place_of_anc_care") val placeOfAncCare: String?,
    @SerializedName("planned_place_delivery") val plannedPlaceDelivery: String?,
    @SerializedName("danger_signs_explained") val dangerSignsExplained: Boolean?,
    @SerializedName("next_visit_date") val nextVisitDate: String?
)

data class AncRecordsResponse(
    val message: String,
    val count: Int,
    @SerializedName("anc_records") val ancRecords: List<AncRecordDto>
)

// ====== POST survey/anc-followup ======

data class AncFollowUpRequest(
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("visit_date") val visitDate: String,
    @SerializedName("visit_number") val visitNumber: Int,
    @SerializedName("facility_type") val facilityType: String,
    @SerializedName("symptom_vaginal_bleeding") val symptomVaginalBleeding: Boolean,
    @SerializedName("symptom_severe_headache") val symptomSevereHeadache: Boolean,
    @SerializedName("symptom_swelling_face_hands") val symptomSwellingFaceHands: Boolean,
    @SerializedName("symptom_fever_chills") val symptomFeverChills: Boolean,
    @SerializedName("symptom_reduced_baby_movement") val symptomReducedBabyMovement: Boolean,
    @SerializedName("symptom_severe_abdominal_pain") val symptomSevereAbdominalPain: Boolean,
    @SerializedName("symptom_none") val symptomNone: Boolean,
    @SerializedName("bp_recorded") val bpRecorded: Boolean,
    @SerializedName("bp_value") val bpValue: String?,
    @SerializedName("weight_kg") val weightKg: Float?,
    @SerializedName("ifa_tablets_given") val ifaTabletsGiven: Int?,
    @SerializedName("calcium_tablets_given") val calciumTabletsGiven: Int?,
    @SerializedName("tt_td_dose") val ttTdDose: String?,
    @SerializedName("referral_made") val referralMade: Boolean,
    @SerializedName("next_visit_date") val nextVisitDate: String?
)

data class AncFollowUpResponse(
    val message: String,
    @SerializedName("followup_id") val followupId: Int
)

// ====== GET survey/anc-followup (optional for future use) ======

data class AncFollowUpRecordDto(
    @SerializedName("followup_id") val followupId: Int?,
    @SerializedName("patient_id") val patientId: String?,
    @SerializedName("visit_date") val visitDate: String?,
    @SerializedName("visit_number") val visitNumber: Int?,
    @SerializedName("facility_type") val facilityType: String?,
    @SerializedName("symptom_vaginal_bleeding") val symptomVaginalBleeding: Boolean?,
    @SerializedName("symptom_severe_headache") val symptomSevereHeadache: Boolean?,
    @SerializedName("symptom_swelling_face_hands") val symptomSwellingFaceHands: Boolean?,
    @SerializedName("symptom_fever_chills") val symptomFeverChills: Boolean?,
    @SerializedName("symptom_reduced_baby_movement") val symptomReducedBabyMovement: Boolean?,
    @SerializedName("symptom_severe_abdominal_pain") val symptomSevereAbdominalPain: Boolean?,
    @SerializedName("symptom_none") val symptomNone: Boolean?,
    @SerializedName("bp_recorded") val bpRecorded: Boolean?,
    @SerializedName("bp_value") val bpValue: String?,
    @SerializedName("weight_kg") val weightKg: Float?,
    @SerializedName("ifa_tablets_given") val ifaTabletsGiven: Int?,
    @SerializedName("calcium_tablets_given") val calciumTabletsGiven: Int?,
    @SerializedName("tt_td_dose") val ttTdDose: String?,
    @SerializedName("referral_made") val referralMade: Boolean?,
    @SerializedName("next_visit_date") val nextVisitDate: String?
)

