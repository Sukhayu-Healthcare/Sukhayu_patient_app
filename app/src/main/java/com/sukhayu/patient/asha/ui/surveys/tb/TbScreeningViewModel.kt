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
import com.sukhayu.patient.data.local.entity.TbScreeningEntity
import com.sukhayu.patient.data.local.entity.toTbFirstRequest
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.repository.TbScreeningRepository
import com.sukhayu.patient.utils.TokenManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TbScreeningViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "TB_SCREENING_VM"
    }

    private val repository: TbScreeningRepository

    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        val db = AshaLocalDatabase.getInstance(application)
        repository = TbScreeningRepository(db.tbScreeningDao())
    }

    /**
     * Save TB screening locally (offline-first).
     * Shows a toast: "TB screening saved on this phone. It will sync when internet is available."
     */
    fun saveTbScreening(entity: TbScreeningEntity) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _errorMessage.value = null

                repository.createOrUpdateTbScreening(entity)

                _isSaving.value = false

                Toast.makeText(
                    getApplication(),
                    "TB screening saved on this phone. It will sync when internet is available.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save TB screening", e)
                _isSaving.value = false
                _errorMessage.value = "Failed to save TB screening: ${e.message}"

                Toast.makeText(
                    getApplication(),
                    "Failed to save TB screening locally.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Sync all unsynced TB screenings to backend.
     * This is called from AshaDashboard when network is available.
     */
    fun syncPendingTbScreenings(onFinished: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val app = getApplication<Application>()

            // Basic network check (extra safety; dashboard also checks)
            val cm = app.getSystemService<ConnectivityManager>()
            val active = cm?.activeNetwork
            val caps = active?.let { cm.getNetworkCapabilities(it) }
            val hasNet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            if (!hasNet) {
                Log.d(TAG, "syncPendingTbScreenings: No internet, skipping sync")
                onFinished(0)
                return@launch
            }

            val token = TokenManager.getToken()
            if (token.isBlank()) {
                Log.e(TAG, "syncPendingTbScreenings: token missing")
                onFinished(0)
                return@launch
            }

            try {
                val pending = repository.getUnsyncedTbScreenings()
                Log.d(TAG, "Found ${pending.size} pending TB screenings to sync")

                if (pending.isEmpty()) {
                    onFinished(0)
                    return@launch
                }

                val api = ApiClient.retrofit
                var successCount = 0

                for (entity in pending) {
                    try {
                        val request = entity.toTbFirstRequest()
                        val response = api.submitTbFirst("Bearer $token", request)
                        Log.d(TAG, "Synced TB screening id=${entity.id}. Response: $response")

                        repository.markAsSynced(entity.id)
                        successCount++
                    } catch (e: HttpException) {
                        val body = e.response()?.errorBody()?.string()
                        Log.e(
                            TAG,
                            "HTTP ${e.code()} while syncing TB screening id=${entity.id}. Body: $body",
                            e
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing TB screening id=${entity.id}", e)
                    }
                }

                if (successCount > 0) {
                    val msg =
                        if (successCount == 1) "1 TB screening synced to server."
                        else "$successCount TB screenings synced to server."
                    Toast.makeText(app, msg, Toast.LENGTH_SHORT).show()
                }

                onFinished(successCount)
            } catch (e: Exception) {
                Log.e(TAG, "syncPendingTbScreenings: error", e)
                onFinished(0)
            }
        }
    }
}
