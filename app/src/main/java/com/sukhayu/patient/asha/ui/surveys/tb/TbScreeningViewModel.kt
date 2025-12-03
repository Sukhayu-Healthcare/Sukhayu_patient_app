package com.sukhayu.patient.asha.ui.surveys.tb

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.entity.TbScreeningEntity
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.TbFirstRequest
import com.sukhayu.patient.data.remote.TbFirstResponse
import com.sukhayu.patient.data.repository.TbScreeningRepository
import com.sukhayu.patient.utils.TokenManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * ViewModel for TB Screening form
 *
 * Responsibilities:
 * - Save TB screening locally (offline-first)
 * - Submit TB screening to backend (POST /tb-first)
 */
class TbScreeningViewModel(
    private val tbScreeningRepository: TbScreeningRepository
) : ViewModel() {

    // ---- Local save state ----
    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving

    private val _saveSuccess = MutableLiveData<Boolean?>()
    val saveSuccess: LiveData<Boolean?> = _saveSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // ---- Backend submit state (POST /tb-first) ----
    private val _submitResult = MutableLiveData<ResultState<TbFirstResponse>>(ResultState.Idle)
    val submitResult: LiveData<ResultState<TbFirstResponse>> = _submitResult

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
     * Submit TB screening to backend (POST /tb-first)
     * Uses TokenManager for auth token and ApiClient.retrofit for API
     */
    fun submitTbFirst(request: TbFirstRequest) {
        viewModelScope.launch {
            val token = TokenManager.getToken()
            if (token.isBlank()) {
                _submitResult.value = ResultState.Error("Authentication token missing")
                return@launch
            }

            _submitResult.value = ResultState.Loading
            try {
                val api = ApiClient.retrofit
                val response = api.submitTbFirst("Bearer $token", request)
                _submitResult.value = ResultState.Success(response)
            } catch (e: HttpException) {
                val body = e.response()?.errorBody()?.string()
                Log.e("TB_FIRST_VM", "HTTP ${e.code()} while submitting TB first. Body: $body", e)
                _submitResult.value = ResultState.Error("Server error: ${e.code()}")
            } catch (e: Exception) {
                Log.e("TB_FIRST_VM", "Error submitting TB first", e)
                _submitResult.value =
                    ResultState.Error(e.message ?: "Unable to submit TB screening")
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
 * Simple Result wrapper for network state
 */
sealed class ResultState<out T> {
    data object Idle : ResultState<Nothing>()
    data object Loading : ResultState<Nothing>()
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val message: String) : ResultState<Nothing>()
}
