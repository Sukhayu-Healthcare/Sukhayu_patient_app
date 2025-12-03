package com.sukhayu.patient.asha.ui.surveys.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.data.repository.AncVisitRepository
import com.sukhayu.patient.data.repository.PregnancyRepository

/**
 * Factory for creating FollowUpAncVisitViewModel with the required repositories.
 */
class FollowUpAncVisitViewModelFactory(
    private val ancVisitRepository: AncVisitRepository,
    private val pregnancyRepository: PregnancyRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FollowUpAncVisitViewModel::class.java)) {
            return FollowUpAncVisitViewModel(ancVisitRepository, pregnancyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

