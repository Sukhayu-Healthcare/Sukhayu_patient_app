package com.sukhayu.patient.asha.ui.surveys.tb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.data.repository.TbScreeningRepository

class TbScreeningViewModelFactory(
    private val tbScreeningRepository: TbScreeningRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TbScreeningViewModel::class.java)) {
            return TbScreeningViewModel(tbScreeningRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

