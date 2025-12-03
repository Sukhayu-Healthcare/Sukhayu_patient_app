package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sukhayu.patient.data.remote.FirstAncVisitRequest
import java.util.UUID

/**
 * Entity representing a pregnancy record in the local database.
 */
@Entity(tableName = "pregnancies")
data class PregnancyEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val womanId: String, // Reference to PatientEntity
    val lmpDate: String?, // Last Menstrual Period date (dd/MM/yyyy)
    val eddDate: String?, // Expected Delivery Date (dd/MM/yyyy)
    val gravida: Int?, // Total pregnancies
    val para: Int?, // Previous deliveries
    val livingChildren: Int?,
    val hasPreviousComplication: Boolean = false,
    val previousComplicationNote: String?,
    val hasSevereBleeding: Boolean = false,
    val hasConvulsionsHistory: Boolean = false,
    val hasHighBpHistory: Boolean = false,
    val seriousIllnesses: String?, // Comma-separated list of illnesses
    val firstAncDate: String?, // First ANC visit date
    val ancPlace: String?, // Govt facility, Private, Not decided
    val deliveryPlace: String?, // Govt, Private, Home, Not decided
    val dangerSignsExplained: Boolean = false,
    val nextVisitDate: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false // For offline-first sync tracking
)

/**
 * Helper function to parse seriousIllnesses string into individual boolean flags
 */
private fun parseSeriousIllness(illnesses: String?, illness: String): Boolean {
    return illnesses?.contains(illness, ignoreCase = true) ?: false
}

/**
 * Helper function to extract "other" illnesses from the comma-separated list
 */
private fun extractOtherIllness(illnesses: String?): String? {
    if (illnesses.isNullOrBlank()) return null

    val knownIllnesses = listOf("diabetes", "high bp", "heart disease", "tb", "hiv")
    val parts = illnesses.split(",").map { it.trim() }
    val others = parts.filter { part ->
        knownIllnesses.none { known -> part.contains(known, ignoreCase = true) }
    }

    return if (others.isNotEmpty()) others.joinToString(", ") else null
}

/**
 * Convert PregnancyEntity to FirstAncVisitRequest for backend submission
 * Maps local Room entity fields to backend API request format
 */
fun PregnancyEntity.toFirstAncVisitRequest(): FirstAncVisitRequest {
    return FirstAncVisitRequest(
        patientId = this.womanId,  // womanId IS patientId
        firstAncVisitDate = this.firstAncDate ?: "",
        lmpDate = this.lmpDate,
        edd = this.eddDate,
        gravida = this.gravida,
        para = this.para,
        livingChildren = this.livingChildren,
        previousSeriousComplication = this.hasPreviousComplication,
        severeBleedingNow = this.hasSevereBleeding,
        convulsions = this.hasConvulsionsHistory,
        highBpEarlier = this.hasHighBpHistory,
        illnessDiabetes = parseSeriousIllness(this.seriousIllnesses, "diabetes"),
        illnessHighBp = parseSeriousIllness(this.seriousIllnesses, "high bp"),
        illnessHeartDisease = parseSeriousIllness(this.seriousIllnesses, "heart disease"),
        illnessTb = parseSeriousIllness(this.seriousIllnesses, "tb"),
        illnessHiv = parseSeriousIllness(this.seriousIllnesses, "hiv"),
        illnessOther = extractOtherIllness(this.seriousIllnesses),
        placeOfAncCare = this.ancPlace,
        plannedPlaceDelivery = this.deliveryPlace,
        dangerSignsExplained = this.dangerSignsExplained,
        nextVisitDate = this.nextVisitDate
    )
}
