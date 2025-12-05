package com.sukhayu.patient.asha.ui.surveys.pregnancy

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.toFirstAncVisitRequest
import com.sukhayu.patient.data.repository.PregnancyRepository
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.utils.TokenManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Sync ViewModel for Pregnancy / First ANC visits.
 *
 * Responsibilities:
 * - Find all unsynced PregnancyEntity records (isSynced = false)
 * - Submit them to backend (POST /survey/anc)
 * - Mark them as synced in local DB
 */
class PregnancySyncViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PREGNANCY_SYNC_VM"
    }

    private val repository: PregnancyRepository

    init {
        val db = AshaLocalDatabase.getInstance(application)
        repository = PregnancyRepository(db.pregnancyDao(), ApiClient.retrofit)
    }

    /**
     * Sync all unsynced pregnancies (First ANC visits) to backend.
     * Called from dashboard when network is available.
     *
     * @param onFinished callback with number of successfully synced records
     */
    fun syncPendingPregnancies(onFinished: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val app = getApplication<Application>()

            // 1) Check network
            val cm = app.getSystemService<ConnectivityManager>()
            val active = cm?.activeNetwork
            val caps = active?.let { cm.getNetworkCapabilities(it) }
            val hasNet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            if (!hasNet) {
                Log.d(TAG, "No internet, skipping pregnancy sync")
                onFinished(0)
                return@launch
            }

            // 2) Get token
            val token = TokenManager.getToken()
            if (token.isBlank()) {
                Log.e(TAG, "Cannot sync pregnancies: token missing")
                onFinished(0)
                return@launch
            }

            try {
                // 3) Get all unsynced pregnancies from Room
                val pending = repository.getUnsyncedPregnancies()
                Log.d(TAG, "Found ${pending.size} pending pregnancies to sync")

                if (pending.isEmpty()) {
                    onFinished(0)
                    return@launch
                }

                val api = ApiClient.retrofit
                var successCount = 0

                // 4) Push each record to backend
                for (entity in pending) {
                    try {
                        val request = entity.toFirstAncVisitRequest()
                        val response = api.submitFirstAncVisit("Bearer $token", request)
                        Log.d(
                            TAG,
                            "Synced pregnancy id=${entity.id}. Response: $response"
                        )

                        // Mark as synced locally
                        repository.markAsSynced(entity.id)
                        successCount++
                    } catch (e: HttpException) {
                        val body = e.response()?.errorBody()?.string()
                        Log.e(
                            TAG,
                            "HTTP ${e.code()} while syncing pregnancy id=${entity.id}. Body: $body",
                            e
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing pregnancy id=${entity.id}", e)
                    }
                }

                // 5) Toast summary for ASHA
                if (successCount > 0) {
                    val msg =
                        if (successCount == 1) "1 pregnancy (ANC first visit) synced to server."
                        else "$successCount pregnancies (ANC first visits) synced to server."
                    Toast.makeText(app, msg, Toast.LENGTH_SHORT).show()
                }

                onFinished(successCount)
            } catch (e: Exception) {
                Log.e(TAG, "Error while syncing pregnancies", e)
                onFinished(0)
            }
        }
    }
}
