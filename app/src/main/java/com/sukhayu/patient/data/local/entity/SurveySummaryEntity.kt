package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lightweight summary row for each survey.
 * Used only for the "View Surveys" screen.
 */
@Entity(
    tableName = "survey_summary",
    indices = [
        Index(value = ["ashaId"]),
        Index(value = ["patientId"]),
        Index(value = ["surveyType"]),
        Index(value = ["surveyDate"])
    ]
)
data class SurveySummaryEntity(

    @PrimaryKey
    val summaryId: String,        // unique row id (UUID)

    val surveyLocalId: String,    // id from the real survey table (TB/ANC/etc.)

    val serverId: Long?,          // id from backend, null until synced

    val patientId: String,
    val patientName: String?,
    val patientPhone: String?,

    val surveyType: String,       // "TB_SCREENING", "ANC_FIRST_VISIT", etc.

    val surveyDate: Long,         // timestamp (System.currentTimeMillis)

    val village: String?,

    val status: String,           // "COMPLETED", "REFERRED", "DRAFT"

    val isSynced: Boolean,        // true = uploaded to server

    val ashaId: String            // which ASHA did this survey
)
