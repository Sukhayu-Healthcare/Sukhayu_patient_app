package com.sukhayu.patient.asha.ui.surveys

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.SurveySummaryEntity
import com.sukhayu.patient.utils.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AshaViewSurveysViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AshaLocalDatabase.getInstance(application).surveySummaryDao()
    private val ashaId: String = TokenManager.getUserId()

    private val _surveys = MutableLiveData<List<SurveySummaryUiModel>>(emptyList())
    val surveys: LiveData<List<SurveySummaryUiModel>> = _surveys

    private val _syncSummaryText = MutableLiveData("Synced: 0 | Pending: 0")
    val syncSummaryText: LiveData<String> = _syncSummaryText

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // LIST (DAO returns Flow<List<SurveySummaryEntity>>)
                val entities: List<SurveySummaryEntity> =
                    dao.getAllForAsha(ashaId).first()
                _surveys.value = entities.map { it.toUiModel() }

                // COUNTS (DAO returns Flow<Long> / Flow<Int>)
                val synced = dao.countSyncedForAsha(ashaId).first().toInt()
                val pending = dao.countPendingForAsha(ashaId).first().toInt()
                _syncSummaryText.value = "Synced: $synced | Pending: $pending"

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load survey summaries"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Map DB entity (nullable fields) → UI model (non-null strings).
     */
    private fun SurveySummaryEntity.toUiModel(): SurveySummaryUiModel {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        // assuming surveyDate is a non-null Long (timestamp in millis)
        val dateString = formatter.format(Date(surveyDate))

        return SurveySummaryUiModel(
            patientName = patientName ?: "Unknown",
            surveyType = friendlyType(surveyType),
            date = dateString,
            village = village ?: "",
            status = status ?: "",
            isSynced = isSynced
        )
    }

    private fun friendlyType(raw: String?): String {
        val value = raw ?: return "Other"
        return when (value) {
            "TB_SCREENING" -> "TB Screening"
            "TB_FOLLOWUP" -> "TB Follow-up"
            "ANC_FIRST_VISIT" -> "ANC First Visit"
            "ANC_FOLLOWUP" -> "ANC Follow-up"
            else -> value
                .lowercase()
                .replace('_', ' ')
                .replaceFirstChar { ch ->
                    if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
                }
        }
    }
}

/**
 * UI model for RecyclerView – all fields non-null for easier binding.
 */
data class SurveySummaryUiModel(
    val patientName: String,
    val surveyType: String,
    val date: String,
    val village: String,
    val status: String,
    val isSynced: Boolean
)
