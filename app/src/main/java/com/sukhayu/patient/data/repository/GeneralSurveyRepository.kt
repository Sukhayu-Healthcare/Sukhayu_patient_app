package com.sukhayu.patient.data.repository

import android.util.Log
import com.sukhayu.patient.data.local.dao.GeneralSurveyDao
import com.sukhayu.patient.data.local.entity.GeneralSurveyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for General Health Survey data operations
 *
 * Handles local database operations and future API sync
 */
class GeneralSurveyRepository(
    private val dao: GeneralSurveyDao
) {
    companion object {
        private const val TAG = "GENERAL_SURVEY_DB"
    }

    /**
     * Insert a new general survey record
     * @return the row ID of the inserted record
     */
    suspend fun insertSurvey(survey: GeneralSurveyEntity): Long = withContext(Dispatchers.IO) {
        Log.d(TAG, "Inserting survey: $survey")

        val rowId = dao.insertGeneralSurvey(survey)

        val count = dao.getGeneralSurveyCount()
        Log.d(TAG, "Survey inserted with ID: $rowId. Total count = $count")

        return@withContext rowId
    }

    /**
     * Get all general surveys
     */
    suspend fun getAllSurveys(): List<GeneralSurveyEntity> = withContext(Dispatchers.IO) {
        dao.getAllGeneralSurveys()
    }

    /**
     * Get surveys for a specific patient
     */
    suspend fun getSurveysForPatient(patientId: String): List<GeneralSurveyEntity> =
        withContext(Dispatchers.IO) {
            dao.getSurveysForPatient(patientId)
        }

    /**
     * Get a specific survey by ID
     */
    suspend fun getSurveyById(surveyId: Long): GeneralSurveyEntity? = withContext(Dispatchers.IO) {
        dao.getSurveyById(surveyId)
    }

    /**
     * Get count of surveys (for debugging)
     */
    suspend fun getSurveyCount(): Int = withContext(Dispatchers.IO) {
        dao.getGeneralSurveyCount()
    }

    /**
     * Get unsynced surveys
     */
    suspend fun getUnsyncedSurveys(): List<GeneralSurveyEntity> = withContext(Dispatchers.IO) {
        dao.getUnsyncedSurveys()
    }

    /**
     * Mark survey as synced
     */
    suspend fun markAsSynced(surveyId: Long) = withContext(Dispatchers.IO) {
        dao.markAsSynced(surveyId)
    }
}

