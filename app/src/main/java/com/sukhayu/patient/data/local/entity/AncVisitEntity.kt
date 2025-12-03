package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sukhayu.patient.data.remote.AncFollowUpRequest
import java.util.UUID

/**
 * Entity representing a Follow-up ANC (Antenatal Care) visit record in the local database.
 * Each visit is linked to a pregnancy record via pregnancyId.
 */
@Entity(tableName = "anc_visits")
data class AncVisitEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // Required fields
    val pregnancyId: String, // Foreign key reference to PregnancyEntity
    val visitNumber: Int, // Visit number (2, 3, 4, ...)
    val visitDate: String, // Visit date in dd/MM/yyyy format

    // Visit location
    val facilityType: String, // GOVT / PRIVATE / HOME

    // Current condition
    val symptomsToday: String?, // Comma-separated list of symptoms

    // Blood pressure
    val bpSystolic: Int?,
    val bpDiastolic: Int?,

    // Physical measurements
    val weightKg: Float?,

    // Interventions
    val ifaTabletsGiven: Int?, // Iron and Folic Acid tablets
    val calciumTabletsGiven: Int?,
    val ttDose: String?, // NONE / FIRST / SECOND / BOOSTER (Tetanus Toxoid)

    // Referral
    val referred: Boolean = false,
    val referralReason: String?,

    // Next visit
    val nextVisitDate: String?, // Optional next visit date in dd/MM/yyyy format

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false // For offline-first sync tracking
)

/**
 * Convert AncVisitEntity to AncFollowUpRequest for backend submission
 * Maps local Room entity fields to backend API request format
 *
 * Note: This requires patientId to be passed separately since AncVisitEntity
 * only stores pregnancyId. The caller (ViewModel or Activity) must provide
 * the patientId from the PregnancyEntity.womanId field.
 */
fun AncVisitEntity.toAncFollowUpRequest(patientId: String): AncFollowUpRequest {
    // Parse symptoms from comma-separated string
    val symptoms = symptomsToday?.split(",")?.map { it.trim() } ?: emptyList()

    // Determine if "NONE" symptom is present
    val hasNoneSymptom = symptoms.any { it.equals("NONE", ignoreCase = true) }

    // If symptom_none is true, all other symptoms should be false
    val symptomVaginalBleeding = !hasNoneSymptom && symptoms.any { it.equals("BLEEDING", ignoreCase = true) }
    val symptomSevereHeadache = !hasNoneSymptom && symptoms.any { it.equals("HEADACHE_BLURRED_VISION", ignoreCase = true) }
    val symptomSwellingFaceHands = !hasNoneSymptom && symptoms.any { it.equals("SWELLING", ignoreCase = true) }
    val symptomFeverChills = !hasNoneSymptom && symptoms.any { it.equals("FEVER_CHILLS", ignoreCase = true) }
    val symptomReducedBabyMovement = !hasNoneSymptom && symptoms.any { it.equals("REDUCED_MOVEMENTS", ignoreCase = true) }
    val symptomSevereAbdominalPain = !hasNoneSymptom && symptoms.any { it.equals("ABDOMINAL_PAIN", ignoreCase = true) }

    // Build BP value string from systolic and diastolic
    val bpRecorded = bpSystolic != null && bpDiastolic != null
    val bpValue = if (bpRecorded) "$bpSystolic/$bpDiastolic" else null

    return AncFollowUpRequest(
        patientId = patientId,
        visitDate = visitDate,
        visitNumber = visitNumber,
        facilityType = facilityType,
        symptomVaginalBleeding = symptomVaginalBleeding,
        symptomSevereHeadache = symptomSevereHeadache,
        symptomSwellingFaceHands = symptomSwellingFaceHands,
        symptomFeverChills = symptomFeverChills,
        symptomReducedBabyMovement = symptomReducedBabyMovement,
        symptomSevereAbdominalPain = symptomSevereAbdominalPain,
        symptomNone = hasNoneSymptom,
        bpRecorded = bpRecorded,
        bpValue = bpValue,
        weightKg = weightKg,
        ifaTabletsGiven = ifaTabletsGiven,
        calciumTabletsGiven = calciumTabletsGiven,
        ttTdDose = ttDose,
        referralMade = referred,
        nextVisitDate = nextVisitDate
    )
}

