package com.sukhayu.patient.asha.ui.surveys.tb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.data.repository.TbFollowUpRepository

class TbFollowUpViewModelFactory(
    private val tbFollowUpRepository: TbFollowUpRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TbFollowUpViewModel::class.java)) {
            return TbFollowUpViewModel(tbFollowUpRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

