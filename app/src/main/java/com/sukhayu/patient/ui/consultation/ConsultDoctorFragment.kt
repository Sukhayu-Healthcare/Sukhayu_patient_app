package com.sukhayu.patient.ui.consultation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sukhayu.patient.DummyData
import com.sukhayu.patient.adapter.DoctorAdapter
import com.sukhayu.patient.databinding.FragmentConsultDoctorBinding
import com.sukhayu.patient.model.Doctor

class ConsultDoctorFragment : Fragment() {

    private var _binding: FragmentConsultDoctorBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var doctorAdapter: DoctorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultDoctorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadDoctors()
    }

    private fun setupRecyclerView() {
        doctorAdapter = DoctorAdapter { doctor ->
            startVideoCall(doctor)
        }
        
        binding.rvDoctors.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = doctorAdapter
        }
    }

    private fun loadDoctors() {
        val doctors = DummyData.getDummyDoctors()
        doctorAdapter.submitList(doctors)
    }

    private fun startVideoCall(doctor: Doctor) {
        // Use actual patient ID from your session/preferences
        val patientId = "patient_${System.currentTimeMillis()}"
        val doctorId = doctor.id
        
        val intent = Intent(requireContext(), VideoCallActivity::class.java).apply {
            putExtra("patientId", patientId)
            putExtra("doctorId", doctorId)
            putExtra("doctorName", doctor.name)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}