package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a TB screening record in the local database.
 *
 * Template ID: "tb_screening_template"
 * Title: "TB Screening / Suspect Form"
 *
 * This entity implements the TB screening template for community screening
 * of adults and adolescents. It follows the same offline-first pattern as
 * pregnancy forms (PregnancyEntity).
 *
 * ASHA layout (TB Screening / Suspect Form):
 * 1) Identification
 *    • Name (text, required)
 *    • Age (years) (number, required)
 *    • Sex (M/F/O) (single_select, required)
 *    • Mobile number (text, optional)
 *    • Address / village (text, required)
 *    • ASHA ID or name (text, required)
 *    • Date of screening (date, required)
 *
 * 2) TB symptom screen (last 2–3 weeks, all Yes/No, all required)
 *    • Cough for 2 weeks or more (boolean, required)
 *    • Cough with blood in sputum (boolean, required)
 *    • Fever for 2 weeks or more (boolean, required)
 *    • Night sweats (boolean, required)
 *    • Weight loss or poor appetite (boolean, required)
 *    • Chest pain or difficulty breathing (boolean, required)
 *    • Anyone in the household currently on TB treatment (boolean, required)
 *
 * 3) Risk factors (Yes/No, optional)
 *    • Previous TB treatment in the past (boolean, optional)
 *    • Close contact of a known TB patient (boolean, optional)
 *    • Known HIV positive (boolean, optional)
 *    • Diabetes (boolean, optional)
 *    • Smoking or tobacco use (boolean, optional)
 *    • Alcohol dependence (boolean, optional)
 *
 * 4) Initial action
 *    • Sputum sample collected? (boolean, optional)
 *    • Sputum collection date (date, optional - if sputum collected)
 *    • Chest X-ray advised? (boolean, optional)
 *    • Referred to PHC / DMC / higher centre? (boolean, optional)
 *    • Referral place name (text, optional - if referred)
 *
 * Field Types Used:
 * - text: For names, addresses, place names
 * - number: For age
 * - date: For dates (dd/MM/yyyy format)
 * - boolean: For yes/no questions (represented as switches in UI)
 * - single_select: For sex (M/F/O)
 *
 * Data Flow:
 * 1. ASHA opens TbSurveyActivity and selects patient
 * 2. ASHA clicks "TB Screening / Suspect Form" button
 * 3. TbScreeningActivity opens with pre-filled patient data
 * 4. ASHA completes the form (2-3 minutes)
 * 5. Form is saved to local database via TbScreeningRepository
 * 6. Data is marked for future sync with NIKSHAY/backend
 */
@Entity(tableName = "tb_screenings")
data class TbScreeningEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // Identification
    val patientId: String, // Reference to PatientEntity
    val name: String,
    val ageYears: Int,
    val sex: String, // M, F, O
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
    val isSynced: Boolean = false // For offline-first sync tracking
    // TODO: Add sync with NIKSHAY/backend when available
)

