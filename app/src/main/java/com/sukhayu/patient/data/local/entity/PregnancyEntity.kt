package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
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

