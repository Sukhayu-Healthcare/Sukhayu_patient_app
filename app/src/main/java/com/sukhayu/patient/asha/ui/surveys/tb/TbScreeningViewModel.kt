package com.sukhayu.patient.asha.ui.surveys.tb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.entity.TbScreeningEntity
import com.sukhayu.patient.data.repository.TbScreeningRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for TB Screening form
 */
class TbScreeningViewModel(
    private val tbScreeningRepository: TbScreeningRepository
) : ViewModel() {

    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving

    private val _saveSuccess = MutableLiveData<Boolean?>()
    val saveSuccess: LiveData<Boolean?> = _saveSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Save TB screening to local database (offline-first)
     */
    fun saveTbScreening(entity: TbScreeningEntity) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _errorMessage.value = null

                tbScreeningRepository.createOrUpdateTbScreening(entity)

                _saveSuccess.value = true
                _isSaving.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save TB screening: ${e.message}"
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

