package com.sukhayu.patient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.remote.AnalyzeRequest
import com.sukhayu.patient.data.remote.AnalyzeResponse
import com.sukhayu.patient.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TriageUiState(
    val isLoading: Boolean = false,
    val complaint: String = "",
    val followupAnswers: List<String> = emptyList(),
    val lastResponse: AnalyzeResponse? = null,
    val errorMessage: String? = null
)

class SymptomViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TriageUiState())
    val uiState: StateFlow<TriageUiState> = _uiState

    fun updateComplaint(text: String) {
        _uiState.value = _uiState.value.copy(complaint = text)
    }

    fun sendComplaintOrFollowup(newAnswer: String? = null) {
        val current = _uiState.value
        val updatedAnswers = if (newAnswer != null) current.followupAnswers + newAnswer else current.followupAnswers

        _uiState.value = current.copy(isLoading = true, followupAnswers = updatedAnswers, errorMessage = null)

        viewModelScope.launch {
            try {
                val request = AnalyzeRequest(current.complaint, updatedAnswers)
                val response = ApiClient.apiServiceAnalyze.analyzeComplaint(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    _uiState.value = current.copy(
                        isLoading = false,
                        lastResponse = body,
                        errorMessage = if (body == null) "Empty response" else null
                    )
                } else {
                    _uiState.value = current.copy(isLoading = false, errorMessage = "Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = current.copy(isLoading = false, errorMessage = "Network error: ${e.message}")
            }
        }
    }
}
