package com.sukhayu.patient.data.local.entity

import java.util.UUID

/**
 * Mapper functions to convert domain entities to SurveySummaryEntity
 * for the "View Surveys" screen.
 */

/**
 * Convert a TbScreeningEntity to a SurveySummaryEntity.
 * This creates a lightweight summary row for the View Surveys list.
 *
 * @param entity The TB screening entity to convert
 * @param ashaId The ASHA worker ID
 * @param isSynced Whether this survey has been synced to the backend
 * @return A SurveySummaryEntity ready to insert into the survey_summary table
 */
fun fromTbScreening(
    entity: TbScreeningEntity,
    ashaId: String,
    isSynced: Boolean = false
): SurveySummaryEntity {
    return SurveySummaryEntity(
        summaryId = UUID.randomUUID().toString(),
        surveyLocalId = entity.id,
        serverId = null,
        patientId = entity.patientId,
        patientName = entity.name,
        patientPhone = entity.mobileNumber,
        surveyType = "TB_SCREENING",
        surveyDate = System.currentTimeMillis(),
        village = entity.addressVillage,
        status = "COMPLETED",
        isSynced = isSynced,
        ashaId = ashaId
    )
}

