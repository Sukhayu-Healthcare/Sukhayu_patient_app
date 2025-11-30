package com.sukhayu.patient.asha.ui.surveys.tb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.data.repository.PatientRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for TB Survey screen
 * Handles patient search and form navigation for TB Screening and TB Treatment Follow-up
 *
 * Key Features:
 * - Uses shared PatientRepository.searchPatients() for offline-first patient search
 * - Supports multiple patient selection via dialog when search returns multiple results
 * - Maintains selected patient state and exposes patient details for UI display
 * - Provides navigation events for TB Screening and TB Treatment Follow-up forms
 * - Template IDs: "tb_screening_template" and "tb_follow_up_template"
 *
 * Pattern:
 * This ViewModel follows the same architecture as PregnancySurveyViewModel,
 * ensuring consistency across the ASHA workflows.
 */
class TbSurveyViewModel(
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    private val _patientDetails = MutableLiveData<PatientUiModel?>()
    val patientDetails: LiveData<PatientUiModel?> = _patientDetails

    private val _navigationEvent = MutableLiveData<Event<NavigationEvent>>()
    val navigationEvent: LiveData<Event<NavigationEvent>> = _navigationEvent

    private val _showPatientChooser = MutableLiveData<Event<List<PatientEntity>>>()
    val showPatientChooser: LiveData<Event<List<PatientEntity>>> = _showPatientChooser

    private var selectedPatient: PatientEntity? = null

    var isPatientLoaded: Boolean = false
        private set

    /**
     * Search for patients using the shared PatientRepository (offline-first)
     */
    fun onLoadPatientClicked(query: String, token: String?) {
        if (query.isBlank()) {
            _uiState.value = UiState.Error("Please enter patient name or phone")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                // Use repository which searches local DB first (offline-first)
                // This works for both Pregnancy/ANC and TB modules
                val results = patientRepository.searchPatients(query, token)

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

    /**
     * Select a patient from search results
     */
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

    /**
     * Navigate to TB Screening form
     */
    fun onTbScreeningClicked() {
        val patient = selectedPatient
        val patientUi = _patientDetails.value

        when {
            !isPatientLoaded || patient == null || patientUi == null -> {
                _uiState.value = UiState.Error("Please load patient details first")
            }
            else -> {
                // TODO: Replace with actual TB Screening form activity once JSON template is ready
                // For now, navigate to FirstAncVisitActivity as placeholder
                val event = NavigationEvent.NavigateToTbScreening(
                    patientId = patient.id,
                    patientName = patientUi.name,
                    patientPhone = patientUi.phone,
                    patientGender = patientUi.gender,
                    patientWeight = patientUi.weight,
                    templateId = TbFormType.TB_SCREENING.templateId
                )
                _navigationEvent.value = Event(event)
            }
        }
    }

    /**
     * Navigate to TB Treatment Follow-up (DOTS) form
     */
    fun onTbFollowUpClicked() {
        val patient = selectedPatient
        val patientUi = _patientDetails.value

        when {
            !isPatientLoaded || patient == null || patientUi == null -> {
                _uiState.value = UiState.Error("Please load patient details first")
            }
            else -> {
                // TODO: Replace with actual TB Follow-up form activity once JSON template is ready
                // For now, navigate to FollowUpAncVisitActivity as placeholder
                val event = NavigationEvent.NavigateToTbFollowUp(
                    patientId = patient.id,
                    patientName = patientUi.name,
                    patientPhone = patientUi.phone,
                    patientGender = patientUi.gender,
                    patientWeight = patientUi.weight,
                    templateId = TbFormType.TB_TREATMENT_FOLLOWUP.templateId
                )
                _navigationEvent.value = Event(event)
            }
        }
    }

    /**
     * Clear error state
     */
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

