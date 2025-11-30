package com.sukhayu.patient.asha.ui.surveys.general_survey

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.GeneralSurveyEntity
import com.sukhayu.patient.data.repository.GeneralSurveyRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for General Survey Activity
 *
 * Manages UI state and coordinates data operations
 */
class GeneralSurveyViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "GENERAL_SURVEY_VM"
    }

    private val repository: GeneralSurveyRepository

    init {
        val database = AshaLocalDatabase.getInstance(application)
        repository = GeneralSurveyRepository(database.generalSurveyDao())
    }

    /**
     * Save a general survey to local database
     */
    fun saveSurvey(
        survey: GeneralSurveyEntity,
        onSuccess: (Long) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "saveSurvey called with patientId: ${survey.patientId}")
                val rowId = repository.insertSurvey(survey)
                Log.d(TAG, "Survey saved successfully with ID: $rowId")
                onSuccess(rowId)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving survey", e)
                onError(e)
            }
        }
    }

    /**
     * Get all surveys (for testing/debugging)
     */
    fun getAllSurveys(callback: (List<GeneralSurveyEntity>) -> Unit) {
        viewModelScope.launch {
            try {
                val surveys = repository.getAllSurveys()
                Log.d(TAG, "Retrieved ${surveys.size} surveys")
                callback(surveys)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching surveys", e)
                callback(emptyList())
            }
        }
    }

    /**
     * Get surveys for a specific patient
     */
    fun getSurveysForPatient(patientId: String, callback: (List<GeneralSurveyEntity>) -> Unit) {
        viewModelScope.launch {
            try {
                val surveys = repository.getSurveysForPatient(patientId)
                Log.d(TAG, "Retrieved ${surveys.size} surveys for patient: $patientId")
                callback(surveys)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching surveys for patient", e)
                callback(emptyList())
            }
        }
    }
}

