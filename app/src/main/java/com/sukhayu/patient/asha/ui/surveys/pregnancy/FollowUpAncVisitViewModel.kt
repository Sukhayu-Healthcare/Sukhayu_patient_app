package com.sukhayu.patient.asha.ui.surveys.pregnancy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.sukhayu.patient.data.local.entity.AncVisitEntity
import com.sukhayu.patient.data.remote.AncFollowUpRequest
import com.sukhayu.patient.data.remote.AncFollowUpResponse
import com.sukhayu.patient.data.repository.AncVisitRepository
import com.sukhayu.patient.data.repository.PregnancyRepository
import com.sukhayu.patient.data.repository.ResultState
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Follow-up ANC Visit form data and saving to the repository.
 * Handles both local DB persistence and backend submission.
 */
class FollowUpAncVisitViewModel(
    private val ancVisitRepository: AncVisitRepository,
    private val pregnancyRepository: PregnancyRepository
) : ViewModel() {

    // ---- Local save state ----
    private val _isSaving = MutableLiveData<Boolean>(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _saveSuccess = MutableLiveData<Boolean>(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // ---- Backend submit state (POST survey/anc-followup) ----
    private val _submitResult = MutableLiveData<ResultState<AncFollowUpResponse>>(ResultState.Idle)
    val submitResult: LiveData<ResultState<AncFollowUpResponse>> = _submitResult

    /**
     * Save an ANC visit record to the local database.
     * The repository will handle marking it as unsynced for later background sync.
     */
    fun saveVisit(visit: AncVisitEntity) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _errorMessage.value = null

                // Call repository to save (repository handles Dispatchers.IO)
                ancVisitRepository.addOrUpdateVisit(visit)

                // Success
                _isSaving.value = false
                _saveSuccess.value = true

            } catch (e: Exception) {
                // Handle failure
                _isSaving.value = false
                _saveSuccess.value = false
                _errorMessage.value = "Failed to save follow-up visit"
                e.printStackTrace()
            }
        }
    }

    /**
     * Submit ANC follow-up to backend (POST survey/anc-followup)
     */
    fun submitAncFollowUp(request: AncFollowUpRequest) {
        viewModelScope.launch {
            _submitResult.value = ResultState.Loading

            try {
                val result = pregnancyRepository.submitAncFollowUp(request)
                _submitResult.value = result
            } catch (e: Exception) {
                Log.e("ANC_FOLLOWUP_VM", "Error submitting ANC follow-up", e)
                _submitResult.value = ResultState.Error(e.message ?: "Unable to submit ANC follow-up")
            }
        }
    }

    /**
     * Reset save state (useful when navigating back to form or retrying).
     */
    fun resetSaveState() {
        _saveSuccess.value = false
        _errorMessage.value = null
    }
}

