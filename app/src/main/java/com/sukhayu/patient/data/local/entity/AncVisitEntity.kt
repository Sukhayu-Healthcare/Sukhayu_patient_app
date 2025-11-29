package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
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

