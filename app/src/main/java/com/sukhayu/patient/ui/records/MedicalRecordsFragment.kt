package com.sukhayu.patient.ui.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sukhayu.patient.databinding.FragmentMedicalRecordsBinding
import com.sukhayu.patient.utils.TtsHelper

class MedicalRecordsFragment : Fragment() {

    private var _binding: FragmentMedicalRecordsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var ttsHelper: TtsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicalRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsHelper = TtsHelper(requireContext())
        ttsHelper.setLanguage("en")

        // TODO: Pass ttsHelper to your adapter
    }

    override fun onDestroyView() {
        ttsHelper.shutdown()
        _binding = null
        super.onDestroyView()
    }
}