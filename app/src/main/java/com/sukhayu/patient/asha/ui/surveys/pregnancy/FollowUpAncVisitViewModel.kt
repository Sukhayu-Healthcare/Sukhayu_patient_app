package com.sukhayu.patient.asha.ui.surveys.pregnancy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.entity.AncVisitEntity
import com.sukhayu.patient.data.repository.AncVisitRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Follow-up ANC Visit form data and saving to the repository.
 */
class FollowUpAncVisitViewModel(
    private val ancVisitRepository: AncVisitRepository
) : ViewModel() {

    private val _isSaving = MutableLiveData<Boolean>(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _saveSuccess = MutableLiveData<Boolean>(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

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
     * Reset save state (useful when navigating back to form or retrying).
     */
    fun resetSaveState() {
        _saveSuccess.value = false
        _errorMessage.value = null
    }
}

