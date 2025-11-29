package com.sukhayu.patient.asha.ui.surveys.pregnancy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.entity.PregnancyEntity
import com.sukhayu.patient.data.repository.PregnancyRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing First ANC Visit form data and saving to the repository.
 */
class FirstAncVisitViewModel(
    private val pregnancyRepository: PregnancyRepository
) : ViewModel() {

    private val _isSaving = MutableLiveData<Boolean>(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _saveSuccess = MutableLiveData<Boolean>(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

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
     * Reset save state (useful when navigating back to form or retrying).
     */
    fun resetSaveState() {
        _saveSuccess.value = false
        _errorMessage.value = null
    }
}

