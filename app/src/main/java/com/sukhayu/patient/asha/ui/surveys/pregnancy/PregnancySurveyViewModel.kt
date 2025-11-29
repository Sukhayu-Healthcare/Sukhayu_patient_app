package com.sukhayu.patient.asha.ui.surveys.pregnancy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.DummyData
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.data.repository.PatientRepository
import kotlinx.coroutines.launch

class PregnancySurveyViewModel(
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    private val _patientDetails = MutableLiveData<PatientUiModel?>()
    val patientDetails: LiveData<PatientUiModel?> = _patientDetails

    private val _selectedSurveyType = MutableLiveData<SurveyType?>()
    val selectedSurveyType: LiveData<SurveyType?> = _selectedSurveyType

    private val _navigationEvent = MutableLiveData<Event<NavigationEvent>>()
    val navigationEvent: LiveData<Event<NavigationEvent>> = _navigationEvent

    private val _showPatientChooser = MutableLiveData<Event<List<PatientEntity>>>()
    val showPatientChooser: LiveData<Event<List<PatientEntity>>> = _showPatientChooser

    private var selectedPatient: PatientEntity? = null

    // TODO: Set to false once backend is stable and working
    var useDummyData: Boolean = false

    var isPatientLoaded: Boolean = false
        private set

    fun onLoadPatientClicked(query: String, token: String?) {
        if (query.isBlank()) {
            _uiState.value = UiState.Error("Please enter patient name or phone")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                val results = if (useDummyData) {
                    // Use dummy data for testing
                    val dummyResults = DummyData.searchDummyPatients(query)
                    if (dummyResults.isEmpty()) {
                        // If no match, create dummy patient with entered name
                        listOf(DummyData.getDummyPatient(query))
                    } else {
                        dummyResults
                    }
                } else {
                    // Use real backend/local DB
                    patientRepository.searchPatients(query, token)
                }

                when {
                    results.isEmpty() -> {
                        _uiState.value = UiState.Error("No patient found")
                        _patientDetails.value = null
                        selectedPatient = null
                        isPatientLoaded = false
                    }
                    results.size == 1 -> {
                        // Exactly one match - auto select
                        selectPatient(results[0])
                        _uiState.value = UiState.Success
                    }
                    else -> {
                        // Multiple matches - show chooser
                        _showPatientChooser.value = Event(results)
                        _uiState.value = UiState.Idle
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error loading patient: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun selectPatient(patient: PatientEntity) {
        selectedPatient = patient
        isPatientLoaded = true
        _patientDetails.value = PatientUiModel(
            id = patient.id,
            name = patient.name,
            phone = patient.phone ?: "—",
            gender = patient.gender ?: "—",
            weight = if (patient.weightKg != null) "${patient.weightKg} kg" else "—"
        )
    }

    fun onSurveyTypeSelected(type: SurveyType) {
        _selectedSurveyType.value = type
    }

    fun onContinueClicked() {
        val patient = selectedPatient
        val surveyType = _selectedSurveyType.value

        when {
            !isPatientLoaded || patient == null -> {
                _uiState.value = UiState.Error("Please load patient details first")
            }
            surveyType == null -> {
                _uiState.value = UiState.Error("Please select survey type")
            }
            else -> {
                val event = when (surveyType) {
                    SurveyType.FIRST_ANC_VISIT ->
                        NavigationEvent.NavigateToFirstAncVisit(patient.id, patient.name)
                    SurveyType.FOLLOW_UP_ANC_VISIT ->
                        NavigationEvent.NavigateToFollowUpAncVisit(patient.id, patient.name)
                }
                _navigationEvent.value = Event(event)
            }
        }
    }

    fun clearError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Idle
        }
    }
}

/**
 * Event wrapper for one-time events
 */
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}

