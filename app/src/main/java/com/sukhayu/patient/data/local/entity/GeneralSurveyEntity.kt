package com.sukhayu.patient.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sukhayu.patient.data.remote.GeneralSurveyRequest

/**
 * Room Entity for General Health Survey
 *
 * Stores general health screening data collected by ASHA workers
 * for community health surveillance and NCD risk assessment.
 */
@Entity(tableName = "general_survey")
data class GeneralSurveyEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    // Patient Information
    @ColumnInfo(name = "patient_id")
    val patientId: String,

    @ColumnInfo(name = "patient_name")
    val patientName: String?,

    // Section 1: Identification
    @ColumnInfo(name = "visit_date")
    val visitDate: String,

    @ColumnInfo(name = "location")
    val location: String?,

    // Section 2: Existing Conditions
    @ColumnInfo(name = "has_diabetes")
    val hasDiabetes: Boolean?,

    @ColumnInfo(name = "has_hypertension")
    val hasHypertension: Boolean?,

    @ColumnInfo(name = "has_heart_disease")
    val hasHeartDisease: Boolean?,

    @ColumnInfo(name = "has_stroke")
    val hasStroke: Boolean?,

    @ColumnInfo(name = "has_kidney_disease")
    val hasKidneyDisease: Boolean?,

    @ColumnInfo(name = "other_conditions")
    val otherConditions: String?,

    // Section 3: Symptoms
    @ColumnInfo(name = "symptom_frequent_urination")
    val symptomFrequentUrination: Boolean?,

    @ColumnInfo(name = "symptom_excessive_thirst")
    val symptomExcessiveThirst: Boolean?,

    @ColumnInfo(name = "symptom_weight_loss")
    val symptomWeightLoss: Boolean?,

    @ColumnInfo(name = "symptom_blurred_vision")
    val symptomBlurredVision: Boolean?,

    @ColumnInfo(name = "symptom_chest_pain")
    val symptomChestPain: Boolean?,

    @ColumnInfo(name = "symptom_shortness_breath")
    val symptomShortnessOfBreath: Boolean?,

    @ColumnInfo(name = "symptom_fatigue")
    val symptomFatigue: Boolean?,

    // Section 4: Risk Factors
    @ColumnInfo(name = "risk_family_history")
    val riskFamilyHistory: Boolean?,

    @ColumnInfo(name = "risk_tobacco_use")
    val riskTobaccoUse: Boolean?,

    @ColumnInfo(name = "risk_alcohol_use")
    val riskAlcoholUse: Boolean?,

    @ColumnInfo(name = "risk_physical_inactivity")
    val riskPhysicalInactivity: Boolean?,

    @ColumnInfo(name = "risk_unhealthy_diet")
    val riskUnhealthyDiet: Boolean?,

    // Section 5: Service Use
    @ColumnInfo(name = "has_regular_checkups")
    val hasRegularCheckups: Boolean?,

    @ColumnInfo(name = "on_current_medication")
    val onCurrentMedication: Boolean?,

    @ColumnInfo(name = "medication_details")
    val medicationDetails: String?,

    @ColumnInfo(name = "had_recent_bp_check")
    val hadRecentBpCheck: Boolean?,

    @ColumnInfo(name = "had_recent_sugar_check")
    val hadRecentSugarCheck: Boolean?,

    // Section 6: ASHA Assessment
    @ColumnInfo(name = "referral_needed")
    val referralNeeded: Boolean?,

    @ColumnInfo(name = "referral_facility")
    val referralFacility: String?,

    @ColumnInfo(name = "remarks")
    val remarks: String?,

    // Metadata
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "synced_to_server")
    val syncedToServer: Boolean = false
)

/**
 * Helper: convert nullable Boolean to backend "yes"/"no"/null
 */
private fun boolToString(value: Boolean?): String? =
    when (value) {
        true -> "yes"
        false -> "no"
        null -> null
    }

/**
 * Map local entity → backend request body for POST /survey/genral
 */
fun GeneralSurveyEntity.toRequest(): GeneralSurveyRequest {
    return GeneralSurveyRequest(
        // visit_date → screening_date
        screeningDate = this.visitDate,

        // location → village (backend expects non-null)
        village = this.location ?: "",

        // Existing conditions
        diabetes = boolToString(this.hasDiabetes),
        hypertension = boolToString(this.hasHypertension),
        heartDisease = boolToString(this.hasHeartDisease),
        stroke = boolToString(this.hasStroke),
        kidneyProblem = boolToString(this.hasKidneyDisease),
        otherCondition = this.otherConditions,

        // Symptoms
        urination = boolToString(this.symptomFrequentUrination),
        thirst = boolToString(this.symptomExcessiveThirst),
        weightLoss = boolToString(this.symptomWeightLoss),
        blurredVision = boolToString(this.symptomBlurredVision),
        chestPain = boolToString(this.symptomChestPain),
        shortnessOfBreath = boolToString(this.symptomShortnessOfBreath),
        weakness = boolToString(this.symptomFatigue),

        // Risk factors
        familyHistory = boolToString(this.riskFamilyHistory),
        pastHistory = null, // not captured in entity yet
        tobacco = boolToString(this.riskTobaccoUse),
        alcohol = boolToString(this.riskAlcoholUse),
        physicalActivity = boolToString(this.riskPhysicalInactivity),
        diet = boolToString(this.riskUnhealthyDiet),

        // Service use
        regularHealthCheck = boolToString(this.hasRegularCheckups),
        currentMedication = boolToString(this.onCurrentMedication),
        medicationDetails = this.medicationDetails,
        bpCheck = boolToString(this.hadRecentBpCheck),
        sugarCheck = boolToString(this.hadRecentSugarCheck),

        // Remarks
        remarks = this.remarks
    )
}
