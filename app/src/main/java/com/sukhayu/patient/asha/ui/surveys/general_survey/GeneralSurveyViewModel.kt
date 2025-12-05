package com.sukhayu.patient.asha.ui.surveys.general_survey

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.GeneralSurveyEntity
import com.sukhayu.patient.data.local.entity.toRequest
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.GeneralScreeningsResponse
import com.sukhayu.patient.data.remote.GeneralSurveyRequest
import com.sukhayu.patient.data.remote.GeneralSurveyResponse
import com.sukhayu.patient.data.repository.GeneralSurveyRepository
import com.sukhayu.patient.data.repository.PatientRepository
import com.sukhayu.patient.utils.TokenManager
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
    private val patientRepository: PatientRepository

    private val _submitResult =
        MutableLiveData<ResultState<GeneralSurveyResponse>>(ResultState.Idle)
    val submitResult: LiveData<ResultState<GeneralSurveyResponse>> = _submitResult

    private val _screenings =
        MutableLiveData<ResultState<GeneralScreeningsResponse>>(ResultState.Idle)
    val screenings: LiveData<ResultState<GeneralScreeningsResponse>> = _screenings

    init {
        val database = AshaLocalDatabase.getInstance(application)
        repository = GeneralSurveyRepository(database.generalSurveyDao())
        patientRepository = PatientRepository(database, ApiClient.retrofit)
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

    /**
     * Submit general survey to backend (POST /survey/genral)
     *
     * NOTE: This submits a single request object.
     * For offline-first flow, prefer syncPendingSurveys() which works off local DB.
     */
    fun submitGeneralSurvey(request: GeneralSurveyRequest) {
        viewModelScope.launch {
            val token = TokenManager.getToken()
            if (token.isBlank()) {
                _submitResult.value = ResultState.Error("Authentication token missing")
                return@launch
            }

            _submitResult.value = ResultState.Loading
            try {
                val response = patientRepository.submitGeneralSurvey(token, request)
                _submitResult.value = ResultState.Success(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting survey", e)
                _submitResult.value =
                    ResultState.Error(e.message ?: "Unable to submit survey")
            }
        }
    }

    /**
     * Load general screenings from backend (GET /survey/genral)
     */
    fun loadGeneralScreenings() {
        viewModelScope.launch {
            val token = TokenManager.getToken()
            if (token.isBlank()) {
                _screenings.value = ResultState.Error("Authentication token missing")
                return@launch
            }

            _screenings.value = ResultState.Loading
            try {
                val response = patientRepository.getGeneralScreenings(token)
                _screenings.value = ResultState.Success(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading screenings", e)
                _screenings.value =
                    ResultState.Error(e.message ?: "Unable to load screenings")
            }
        }
    }

    /**
     * Sync all pending (unsynced) general surveys from local DB to backend.
     *
     * - Reads all records where synced_to_server = 0
     * - Converts them to GeneralSurveyRequest using toRequest()
     * - Calls POST /survey/genral for each
     * - Marks them as synced in local DB on success
     */
    fun syncPendingSurveys(onFinished: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val token = TokenManager.getToken()
            if (token.isBlank()) {
                Log.e(TAG, "Cannot sync general surveys: Authentication token missing")
                onFinished(0)
                return@launch
            }

            try {
                val pending = repository.getUnsyncedSurveys()
                Log.d(TAG, "Found ${pending.size} pending general surveys to sync")

                var successCount = 0

                for (survey in pending) {
                    try {
                        val request: GeneralSurveyRequest = survey.toRequest()
                        val response = patientRepository.submitGeneralSurvey(token, request)

                        Log.d(
                            TAG,
                            "Synced general survey id=${survey.id} successfully. Response: $response"
                        )

                        repository.markAsSynced(survey.id)
                        successCount++
                    } catch (e: Exception) {
                        // Keep it unsynced for future retry
                        Log.e(TAG, "Failed to sync general survey id=${survey.id}", e)
                    }
                }

                Log.d(TAG, "General survey sync finished. Successfully synced = $successCount")
                onFinished(successCount)
            } catch (e: Exception) {
                Log.e(TAG, "Error while syncing pending general surveys", e)
                onFinished(0)
            }
        }
    }
}

/**
 * Simple Result wrapper for network state
 */
sealed class ResultState<out T> {
    data object Idle : ResultState<Nothing>()
    data object Loading : ResultState<Nothing>()
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val message: String) : ResultState<Nothing>()
}
