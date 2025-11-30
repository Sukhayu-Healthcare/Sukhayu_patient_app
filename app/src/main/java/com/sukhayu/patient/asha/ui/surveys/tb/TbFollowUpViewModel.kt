package com.sukhayu.patient.asha.ui.surveys.tb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.entity.TbFollowUpEntity
import com.sukhayu.patient.data.repository.TbFollowUpRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for TB Treatment Follow-up (DOTS) form
 */
class TbFollowUpViewModel(
    private val tbFollowUpRepository: TbFollowUpRepository
) : ViewModel() {

    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving

    private val _saveSuccess = MutableLiveData<Boolean?>()
    val saveSuccess: LiveData<Boolean?> = _saveSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Save TB follow-up to local database (offline-first)
     */
    fun saveTbFollowUp(entity: TbFollowUpEntity) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _errorMessage.value = null

                tbFollowUpRepository.saveFollowUp(entity)

                _saveSuccess.value = true
                _isSaving.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save TB follow-up: ${e.message}"
                _isSaving.value = false
                _saveSuccess.value = false
                e.printStackTrace()
            }
        }
    }

    /**
     * Reset save success state
     */
    fun resetSaveState() {
        _saveSuccess.value = null
    }
}

