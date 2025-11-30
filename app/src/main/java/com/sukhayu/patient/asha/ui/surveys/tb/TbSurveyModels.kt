package com.sukhayu.patient.asha.ui.surveys.tb

/**
 * TB form types for navigation
 */
enum class TbFormType(val displayName: String, val templateId: String) {
    TB_SCREENING("TB Screening / Suspect Form", "tb_screening_template"),
    TB_TREATMENT_FOLLOWUP("TB Treatment Follow-up (DOTS)", "tb_follow_up_template");
}

/**
 * Patient UI model for displaying selected patient details
 */
data class PatientUiModel(
    val id: String,
    val name: String,
    val phone: String,
    val gender: String,
    val weight: String
)

/**
 * UI state for the TB Survey screen
 */
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Error(val message: String) : UiState()
    object Success : UiState()
}

/**
 * Navigation events for TB forms
 */
sealed class NavigationEvent {
    data class NavigateToTbScreening(
        val patientId: String,
        val patientName: String,
        val patientPhone: String,
        val patientGender: String,
        val patientWeight: String,
        val templateId: String
    ) : NavigationEvent()

    data class NavigateToTbFollowUp(
        val patientId: String,
        val patientName: String,
        val patientPhone: String,
        val patientGender: String,
        val patientWeight: String,
        val templateId: String
    ) : NavigationEvent()
}

