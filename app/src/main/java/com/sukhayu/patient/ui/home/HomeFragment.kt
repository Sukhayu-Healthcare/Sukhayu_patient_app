package com.sukhayu.patient.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sukhayu.patient.R
import com.sukhayu.patient.databinding.FragmentHomeBinding
import com.sukhayu.patient.ui.consultation.VideoCallActivity
import com.sukhayu.patient.utils.TtsHelper

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var ttsHelper: TtsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsHelper = TtsHelper(requireContext())
        ttsHelper.setLanguage("en")

        binding.cardVideoConsultation?.setOnClickListener {
            startVideoCall()
        }
    }

    private fun startVideoCall() {
        val patientId = "patient_${System.currentTimeMillis()}"
        val intent = Intent(requireContext(), VideoCallActivity::class.java).apply {
            putExtra("patientId", patientId)
            putExtra("doctorId", "doctor_default")
            putExtra("doctorName", "Available Doctor")
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        ttsHelper.shutdown()
        _binding = null
        super.onDestroyView()
    }
}