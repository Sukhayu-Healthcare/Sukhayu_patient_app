package com.sukhayu.patient.asha.ui.surveys.tb

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.entity.TbFollowUpEntity
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.TbFollowUpRequest
import com.sukhayu.patient.data.remote.TbFollowUpResponse
import com.sukhayu.patient.data.repository.TbFollowUpRepository
import com.sukhayu.patient.utils.TokenManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * ViewModel for TB Treatment Follow-up (DOTS) form
 *
 * Responsibilities:
 * - Save TB follow-up locally (offline-first)
 * - Submit TB follow-up to backend (POST survey/tb-followup)
 */
class TbFollowUpViewModel(
    private val tbFollowUpRepository: TbFollowUpRepository
) : ViewModel() {

    // ---- Local save state ----
    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving

    private val _saveSuccess = MutableLiveData<Boolean?>()
    val saveSuccess: LiveData<Boolean?> = _saveSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // ---- Backend submit state (POST survey/tb-followup) ----
    private val _submitResult = MutableLiveData<ResultState<TbFollowUpResponse>>(ResultState.Idle)
    val submitResult: LiveData<ResultState<TbFollowUpResponse>> = _submitResult

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
     * Submit TB follow-up to backend (POST survey/tb-followup)
     * Uses TokenManager for auth token and ApiClient.retrofit for API
     */
    fun submitTbFollowUp(request: TbFollowUpRequest) {
        viewModelScope.launch {
            val token = TokenManager.getToken()
            if (token.isBlank()) {
                _submitResult.value = ResultState.Error("Authentication token missing")
                return@launch
            }

            _submitResult.value = ResultState.Loading
            try {
                val api = ApiClient.retrofit
                val response = api.submitTbFollowUp("Bearer $token", request)
                _submitResult.value = ResultState.Success(response)
            } catch (e: HttpException) {
                val body = e.response()?.errorBody()?.string()
                Log.e("TB_FOLLOWUP_VM", "HTTP ${e.code()} while submitting TB follow-up. Body: $body", e)
                _submitResult.value = ResultState.Error("Server error: ${e.code()}")
            } catch (e: Exception) {
                Log.e("TB_FOLLOWUP_VM", "Error submitting TB follow-up", e)
                _submitResult.value = ResultState.Error(e.message ?: "Unable to submit TB follow-up")
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

/**
 * Generic sealed class to represent result states for backend operations
 */
sealed class ResultState<out T> {
    object Idle : ResultState<Nothing>()
    object Loading : ResultState<Nothing>()
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val message: String) : ResultState<Nothing>()
}
