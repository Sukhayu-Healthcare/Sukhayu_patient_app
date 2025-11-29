package com.sukhayu.patient.asha.ui.surveys.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.data.repository.PatientRepository

class PregnancySurveyViewModelFactory(
    private val patientRepository: PatientRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PregnancySurveyViewModel::class.java)) {
            return PregnancySurveyViewModel(patientRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

