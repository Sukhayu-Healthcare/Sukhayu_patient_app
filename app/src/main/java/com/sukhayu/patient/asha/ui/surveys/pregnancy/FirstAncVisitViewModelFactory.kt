package com.sukhayu.patient.asha.ui.surveys.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.data.repository.PregnancyRepository

/**
 * Factory for creating FirstAncVisitViewModel with the required PregnancyRepository dependency.
 */
class FirstAncVisitViewModelFactory(
    private val repository: PregnancyRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FirstAncVisitViewModel::class.java)) {
            return FirstAncVisitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

