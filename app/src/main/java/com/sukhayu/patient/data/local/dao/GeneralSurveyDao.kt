package com.sukhayu.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sukhayu.patient.data.local.entity.GeneralSurveyEntity

/**
 * DAO for General Health Survey database operations
 */
@Dao
interface GeneralSurveyDao {

    /**
     * Insert a new general survey record
     * @return the row ID of the inserted record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneralSurvey(survey: GeneralSurveyEntity): Long

    /**
     * Get all general survey records
     */
    @Query("SELECT * FROM general_survey ORDER BY created_at DESC")
    suspend fun getAllGeneralSurveys(): List<GeneralSurveyEntity>

    /**
     * Get general surveys for a specific patient
     */
    @Query("SELECT * FROM general_survey WHERE patient_id = :patientId ORDER BY created_at DESC")
    suspend fun getSurveysForPatient(patientId: String): List<GeneralSurveyEntity>

    /**
     * Get a specific general survey by ID
     */
    @Query("SELECT * FROM general_survey WHERE id = :surveyId")
    suspend fun getSurveyById(surveyId: Long): GeneralSurveyEntity?

    /**
     * Get count of all general survey records (for debugging)
     */
    @Query("SELECT COUNT(*) FROM general_survey")
    suspend fun getGeneralSurveyCount(): Int

    /**
     * Get unsynced surveys
     */
    @Query("SELECT * FROM general_survey WHERE synced_to_server = 0 ORDER BY created_at ASC")
    suspend fun getUnsyncedSurveys(): List<GeneralSurveyEntity>

    /**
     * Mark survey as synced
     */
    @Query("UPDATE general_survey SET synced_to_server = 1 WHERE id = :surveyId")
    suspend fun markAsSynced(surveyId: Long)

    /**
     * Delete all general surveys (for testing/debugging)
     */
    @Query("DELETE FROM general_survey")
    suspend fun deleteAllSurveys()
}
