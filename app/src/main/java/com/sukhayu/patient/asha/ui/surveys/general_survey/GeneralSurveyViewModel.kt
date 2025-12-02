package com.sukhayu.patient.asha.ui.surveys.general_survey

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.GeneralSurveyEntity
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

    private val _submitResult = MutableLiveData<ResultState<GeneralSurveyResponse>>(ResultState.Idle)
    val submitResult: LiveData<ResultState<GeneralSurveyResponse>> = _submitResult

    private val _screenings = MutableLiveData<ResultState<GeneralScreeningsResponse>>(ResultState.Idle)
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

    fun submitGeneralSurvey(request: GeneralSurveyRequest) {
        viewModelScope.launch {
            val token = TokenManager.getToken()
            if (token.isBlank()) {
                _submitResult.value = ResultState.Error("Authentication token missing")
                return@launch
            }

            _submitResult.value = ResultState.Loading
            try {
                Log.d("GENERAL_SURVEY_VM", "Submitting survey with token length=${token.length}")
                Log.d("GENERAL_SURVEY_VM", "Request body = $request")

                val response = patientRepository.submitGeneralSurvey(token, request)
                _submitResult.value = ResultState.Success(response)
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("GENERAL_SURVEY_VM", "HTTP ${e.code()} while submitting survey. Body: $errorBody", e)
                _submitResult.value = ResultState.Error("Server rejected survey: ${e.code()}")
            } catch (e: Exception) {
                Log.e("GENERAL_SURVEY_VM", "Error submitting survey", e)
                _submitResult.value = ResultState.Error(e.message ?: "Unable to submit survey")
            }
        }
    }


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
                _screenings.value = ResultState.Error(e.message ?: "Unable to load screenings")
            }
        }
    }
}

sealed class ResultState<out T> {
    data object Idle : ResultState<Nothing>()
    data object Loading : ResultState<Nothing>()
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val message: String) : ResultState<Nothing>()
}
