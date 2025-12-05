package com.sukhayu.patient.asha.ui.surveys.pregnancy

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.PregnancyEntity
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.repository.PregnancyRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for First ANC Visit - OFFLINE FIRST
 *
 * Responsibilities:
 * - Save PregnancyEntity locally (Room)
 * - Mark it as unsynced; dashboard will sync later
 */
class FirstAncVisitViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "FIRST_ANC_VM"
    }

    private val repository: PregnancyRepository

    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        // Build repository here using Application context
        val db = AshaLocalDatabase.getInstance(application)
        val apiService = ApiClient.retrofit
        repository = PregnancyRepository(db.pregnancyDao(), apiService)
    }

    /**
     * Save a pregnancy record to the local database.
     * Repository marks it as unsynced for later background sync.
     */
    fun savePregnancy(pregnancy: PregnancyEntity) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _errorMessage.value = null

                repository.createOrUpdatePregnancy(pregnancy)

                _isSaving.value = false

                Toast.makeText(
                    getApplication(),
                    "First ANC visit saved on this phone. It will sync when internet is available.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save First ANC visit", e)
                _isSaving.value = false
                _errorMessage.value = "Failed to save First ANC visit: ${e.message}"

                Toast.makeText(
                    getApplication(),
                    "Failed to save First ANC visit locally.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
