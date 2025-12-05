package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sukhayu.patient.data.remote.TbFollowUpRequest
import java.util.UUID

/**
 * Entity representing a TB treatment follow-up (DOTS) record in the local database.
 *
 * Template ID: "tb_follow_up_template"
 */
@Entity(tableName = "tb_follow_ups")
data class TbFollowUpEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // Link to patient
    val patientId: String, // Reference to PatientEntity
    val templateId: String = "tb_follow_up_template",

    // a) Visit details
    val tbPatientIdOrNikshay: String, // TB patient ID / NIKSHAY ID
    val visitDate: String, // dd/MM/yyyy format
    val phaseOfTreatment: String, // "Intensive" or "Continuation"
    val visitType: String, // "Home visit" or "Facility visit"

    // b) Adherence and symptoms
    val dosesMissedSinceLastVisit: Int, // Number of doses missed, can be 0
    val vomitingAfterMedicines: Boolean,
    val yellowEyesOrSkin: Boolean, // Jaundice indicator
    val severeSkinRashOrItching: Boolean,
    val jointPain: Boolean,
    val persistentCoughOrBreathlessness: Boolean,
    val feverLastWeek: Boolean,
    val weightKg: Double, // Current weight in kg

    // c) Programmatic details
    val dotProvider: String, // "ASHA", "Family member", "Health worker", "Other"
    val drugBoxCheckedAndConsistent: Boolean,
    val counsellingGiven: Boolean = false, // Optional

    // d) Decision / action
    val treatmentContinuedAsPlanned: Boolean,
    val referredForSideEffects: Boolean = false,
    val referralReason: String?, // Only if referredForSideEffects is true
    val nextFollowUpDate: String, // dd/MM/yyyy format

    // Metadata for sync
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false // For offline-first sync tracking
)

/**
 * Convert TbFollowUpEntity to TbFollowUpRequest for backend submission
 */
fun TbFollowUpEntity.toBackendRequest(): TbFollowUpRequest {
    return TbFollowUpRequest(
        tbId = this.tbPatientIdOrNikshay,
        patientId = this.patientId,
        visitDate = this.visitDate,
        phaseOfTreatment = this.phaseOfTreatment,
        visitType = this.visitType,
        dosesMissed = this.dosesMissedSinceLastVisit,
        vomiting = this.vomitingAfterMedicines,
        jaundice = this.yellowEyesOrSkin,
        skinRash = this.severeSkinRashOrItching,
        jointPain = this.jointPain,
        persistentCough = this.persistentCoughOrBreathlessness,
        fever = this.feverLastWeek,
        weightThisVisit = this.weightKg,
        dotProvider = this.dotProvider,
        drugBoxChecked = this.drugBoxCheckedAndConsistent,
        counsellingGiven = this.counsellingGiven,
        treatmentContinued = this.treatmentContinuedAsPlanned,
        referredForSideeffects = this.referredForSideEffects,
        nextFollowupDate = this.nextFollowUpDate
    )
}
