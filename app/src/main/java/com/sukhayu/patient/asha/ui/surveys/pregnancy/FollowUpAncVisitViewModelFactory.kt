package com.sukhayu.patient.asha.ui.surveys.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.data.repository.AncVisitRepository

/**
 * Factory for creating FollowUpAncVisitViewModel with the required AncVisitRepository dependency.
 */
class FollowUpAncVisitViewModelFactory(
    private val repository: AncVisitRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FollowUpAncVisitViewModel::class.java)) {
            return FollowUpAncVisitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

