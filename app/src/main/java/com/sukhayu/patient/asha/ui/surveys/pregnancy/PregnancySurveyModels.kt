package com.sukhayu.patient.asha.ui.surveys.pregnancy

enum class SurveyType(val displayName: String) {
    FIRST_ANC_VISIT("First ANC Visit"),
    FOLLOW_UP_ANC_VISIT("Follow-up ANC Visit");

    companion object {
        fun fromDisplayName(name: String?): SurveyType? {
            return values().find { it.displayName == name }
        }
    }
}

data class PatientUiModel(
    val id: String,
    val name: String,
    val phone: String,
    val gender: String,
    val weight: String
)

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Error(val message: String) : UiState()
    object Success : UiState()
}

sealed class NavigationEvent {
    data class NavigateToFirstAncVisit(val patientId: String, val patientName: String) : NavigationEvent()
    data class NavigateToFollowUpAncVisit(val patientId: String, val patientName: String) : NavigationEvent()
}

