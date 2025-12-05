package com.sukhayu.patient.asha.ui.surveys.pregnancy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.sukhayu.patient.data.local.entity.PregnancyEntity
import com.sukhayu.patient.data.remote.FirstAncVisitRequest
import com.sukhayu.patient.data.remote.FirstAncVisitResponse
import com.sukhayu.patient.data.repository.PregnancyRepository
import com.sukhayu.patient.data.repository.ResultState
import kotlinx.coroutines.launch

/**
 * ViewModel for managing First ANC Visit form data and saving to the repository.
 *
 * Responsibilities:
 * - Save pregnancy record locally (offline-first)
 * - Submit First ANC Visit to backend (POST survey/anc)
 */
class FirstAncVisitViewModel(
    private val pregnancyRepository: PregnancyRepository
) : ViewModel() {

    // ---- Local save state ----
    private val _isSaving = MutableLiveData<Boolean>(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _saveSuccess = MutableLiveData<Boolean>(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // ---- Backend submit state (POST survey/anc) ----
    private val _submitResult = MutableLiveData<ResultState<FirstAncVisitResponse>>(ResultState.Idle)
    val submitResult: LiveData<ResultState<FirstAncVisitResponse>> = _submitResult

    /**
     * Save a pregnancy record to the local database.
     * The repository will handle marking it as unsynced for later background sync.
     */
    fun savePregnancy(pregnancy: PregnancyEntity) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _errorMessage.value = null

                // Call repository to save (repository handles Dispatchers.IO)
                pregnancyRepository.createOrUpdatePregnancy(pregnancy)

                // Success
                _isSaving.value = false
                _saveSuccess.value = true

            } catch (e: Exception) {
                // Handle failure
                _isSaving.value = false
                _saveSuccess.value = false
                _errorMessage.value = "Failed to save ANC visit"
                e.printStackTrace()
            }
        }
    }

    /**
     * Submit First ANC Visit to backend (POST survey/anc)
     * Uses repository method that handles token and API call
     */
    fun submitFirstAncVisit(request: FirstAncVisitRequest) {
        viewModelScope.launch {
            _submitResult.value = ResultState.Loading

            try {
                val result = pregnancyRepository.submitFirstAncVisit(request)
                _submitResult.value = result
            } catch (e: Exception) {
                Log.e("FIRST_ANC_VM", "Error submitting first ANC visit", e)
                _submitResult.value = ResultState.Error(e.message ?: "Unable to submit First ANC visit")
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

