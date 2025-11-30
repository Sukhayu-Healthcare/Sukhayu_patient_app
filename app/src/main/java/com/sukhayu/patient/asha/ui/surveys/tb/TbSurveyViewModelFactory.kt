package com.sukhayu.patient.asha.ui.surveys.tb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.data.repository.PatientRepository

class TbSurveyViewModelFactory(
    private val patientRepository: PatientRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TbSurveyViewModel::class.java)) {
            return TbSurveyViewModel(patientRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

