package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import com.sukhayu.patient.data.remote.TbFirstRequest

/**
 * Entity representing a TB screening record in the local database.
 */
@Entity(tableName = "tb_screenings")
data class TbScreeningEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // Identification
    val patientId: String, // Reference to PatientEntity
    val name: String,
    val ageYears: Int,
    val sex: String, // "Male", "Female", "O"
    val mobileNumber: String?,
    val addressVillage: String,
    val ashaIdOrName: String,
    val dateOfScreening: String, // dd/MM/yyyy format

    // TB symptom screen (last 2–3 weeks) - all required
    val cough2WeeksOrMore: Boolean,
    val coughWithBlood: Boolean,
    val fever2WeeksOrMore: Boolean,
    val nightSweats: Boolean,
    val weightLossPoorAppetite: Boolean,
    val chestPainOrDifficultyBreathing: Boolean,
    val householdMemberOnTbTreatment: Boolean,

    // Risk factors - optional
    val previousTbTreatment: Boolean = false,
    val closeContactTbPatient: Boolean = false,
    val knownHivPositive: Boolean = false,
    val diabetes: Boolean = false,
    val smokingOrTobaccoUse: Boolean = false,
    val alcoholDependence: Boolean = false,

    // Initial action
    val sputumCollected: Boolean = false,
    val sputumCollectionDate: String?, // dd/MM/yyyy format, if sputumCollected is true
    val chestXrayAdvised: Boolean = false,
    val referredToHigherCentre: Boolean = false,
    val referralPlaceName: String?, // if referredToHigherCentre is true

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Offline-first sync tracking
    val isSynced: Boolean = false
)

// Helper: Boolean? → "yes" / "no" / null
private fun boolToYesNo(value: Boolean?): String? = when (value) {
    true -> "yes"
    false -> "no"
    null -> null
}

// Map local Room entity → backend request body for POST /survey/tb-first
fun TbScreeningEntity.toTbFirstRequest(): TbFirstRequest {
    return TbFirstRequest(
        patientId = patientId,
        patientName = name,
        age = ageYears,
        gender = sex,
        mobile = mobileNumber,
        address = addressVillage,
        screeningDate = dateOfScreening,

        cough2Weeks = boolToYesNo(cough2WeeksOrMore),
        coughBlood = boolToYesNo(coughWithBlood),
        fever2Weeks = boolToYesNo(fever2WeeksOrMore),
        nightSweats = boolToYesNo(nightSweats),
        weightLoss = boolToYesNo(weightLossPoorAppetite),
        chestPain = boolToYesNo(chestPainOrDifficultyBreathing),

        householdTb = boolToYesNo(householdMemberOnTbTreatment),
        previousTb = boolToYesNo(previousTbTreatment),
        closeContactTb = boolToYesNo(closeContactTbPatient),
        hivPositive = boolToYesNo(knownHivPositive),
        diabetes = boolToYesNo(diabetes),
        tobaccoUse = boolToYesNo(smokingOrTobaccoUse),
        alcoholDependence = boolToYesNo(alcoholDependence),

        sputumCollected = boolToYesNo(sputumCollected),
        chestXray = boolToYesNo(chestXrayAdvised),
        referredToHigherCenter = boolToYesNo(referredToHigherCentre)
    )
}
