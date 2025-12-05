package com.sukhayu.patient.asha.ui.surveys.tb

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.TbFollowUpEntity
import com.sukhayu.patient.data.local.entity.toBackendRequest
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.repository.TbFollowUpRepository
import com.sukhayu.patient.utils.TokenManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * ViewModel for TB Treatment Follow-up (DOTS) form
 *
 * Responsibilities:
 * - Save TB follow-up locally (offline-first)
 * - Sync pending follow-ups to backend (POST /survey/tb-followup)
 */
class TbFollowUpViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "TB_FOLLOW_UP_VM"
    }

    private val repository: TbFollowUpRepository

    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        val db = AshaLocalDatabase.getInstance(application)
        repository = TbFollowUpRepository(db.tbFollowUpDao())
    }

    /**
     * Save TB follow-up to local database (offline-first).
     */
    fun saveTbFollowUp(entity: TbFollowUpEntity) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _errorMessage.value = null

                repository.saveFollowUp(entity)

                _isSaving.value = false

                Toast.makeText(
                    getApplication(),
                    "TB follow-up saved on this phone. It will sync when internet is available.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save TB follow-up", e)
                _isSaving.value = false
                _errorMessage.value = "Failed to save TB follow-up: ${e.message}"

                Toast.makeText(
                    getApplication(),
                    "Failed to save TB follow-up locally.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Sync all unsynced TB follow-ups to backend.
     * Called from dashboard when network is available.
     */
    fun syncPendingTbFollowUps(onFinished: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val app = getApplication<Application>()

            // Network check
            val cm = app.getSystemService<ConnectivityManager>()
            val active = cm?.activeNetwork
            val caps = active?.let { cm.getNetworkCapabilities(it) }
            val hasNet =
                caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            if (!hasNet) {
                Log.d(TAG, "No internet, skipping TB follow-up sync")
                onFinished(0)
                return@launch
            }

            val token = TokenManager.getToken()
            if (token.isBlank()) {
                Log.e(TAG, "Cannot sync TB follow-ups: token missing")
                onFinished(0)
                return@launch
            }

            try {
                val pending = repository.getUnsyncedFollowUps()
                Log.d(TAG, "Found ${pending.size} pending TB follow-ups to sync")

                if (pending.isEmpty()) {
                    onFinished(0)
                    return@launch
                }

                val api = ApiClient.retrofit
                var successCount = 0

                for (entity in pending) {
                    try {
                        val request = entity.toBackendRequest()
                        val response = api.submitTbFollowUp("Bearer $token", request)
                        Log.d(TAG, "Synced follow-up id=${entity.id}. Response: $response")

                        repository.markAsSynced(entity.id)
                        successCount++
                    } catch (e: HttpException) {
                        val body = e.response()?.errorBody()?.string()
                        Log.e(
                            TAG,
                            "HTTP ${e.code()} while syncing follow-up id=${entity.id}. Body: $body",
                            e
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing follow-up id=${entity.id}", e)
                    }
                }

                if (successCount > 0) {
                    val msg =
                        if (successCount == 1) "1 TB follow-up synced to server."
                        else "$successCount TB follow-ups synced to server."
                    Toast.makeText(app, msg, Toast.LENGTH_SHORT).show()
                }

                onFinished(successCount)
            } catch (e: Exception) {
                Log.e(TAG, "Error while syncing TB follow-ups", e)
                onFinished(0)
            }
        }
    }
}
