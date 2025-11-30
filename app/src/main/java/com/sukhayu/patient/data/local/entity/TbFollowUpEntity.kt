package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a TB treatment follow-up (DOTS) record in the local database.
 *
 * Template ID: "tb_follow_up_template"
 * Title: "TB Treatment Follow-up (DOTS)"
 *
 * This entity tracks directly observed treatment (DOTS) follow-up visits for TB patients
 * during both intensive and continuation phases of treatment. Used by ASHAs to monitor
 * adherence, detect side effects, and ensure treatment completion.
 *
 * ASHA layout (TB Treatment Follow-up):
 * a) Visit details
 *    • TB patient ID / NIKSHAY ID (text, required)
 *    • Date of visit (date, required)
 *    • Phase of treatment (Intensive/Continuation, required)
 *    • Visit type (Home visit/Facility visit, required)
 *
 * b) Adherence and symptoms
 *    • Doses missed since last visit (number, required)
 *    • Any vomiting after medicines (boolean, required)
 *    • Yellow eyes/skin (jaundice) (boolean, required)
 *    • Severe skin rash or itching (boolean, required)
 *    • Joint pain (boolean, required)
 *    • Persistent cough or breathlessness (boolean, required)
 *    • Fever in the last week (boolean, required)
 *    • Weight (kg) this visit (number, required)
 *
 * c) Programmatic details
 *    • DOT provider (ASHA/Family member/Health worker/Other, required)
 *    • Drug box checked and consistent (boolean, required)
 *    • Counselling given (boolean, optional)
 *
 * d) Decision / action
 *    • Treatment continued as planned (boolean, required)
 *    • Referred for side effects (boolean, optional)
 *    • Referral reason (text, optional - if referred)
 *    • Next follow-up date (date, required)
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
    // TODO: Implement sync with NIKSHAY/backend when available
)

