package com.sukhayu.patient.data.remote

/**
 * Models used by the Supervisor "View Surveys & Drives" screen.
 * All fields are nullable so JSON changes from backend won't crash the app.
 */
data class SupervisorSurveyDataResponse(
    val table: String,
    val date: String,
    val count: Int,
    val asha_count: Int,
    val records: List<Map<String, Any>>
)

/**
 * One group/entry that wraps a list of survey records.
 * (Shape based on how ViewSurveysAndDrivesActivity uses `data` and `records`.)
 */
data class SupervisorSurveyGroup(
    val records: List<SupervisorSurveyRecord>? = null
)

/**
 * A single survey record shown in the list.
 * Keep everything nullable & generic; the Activity will
 * only use whatever fields it needs.
 */
data class SupervisorSurveyRecord(
    val id: Int? = null,
    val patient_name: String? = null,
    val survey_type: String? = null,
    val survey_date: String? = null,
    val asha_name: String? = null,
    val village: String? = null
)



