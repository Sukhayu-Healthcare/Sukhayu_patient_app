package com.sukhayu.patient.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sukhayu.patient.DummyData
import com.sukhayu.patient.databinding.FragmentDashboardBinding
import com.sukhayu.patient.ui.consultation.VideoCallActivity

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var doctorAdapter: DoctorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDoctorsList()
        setupQuickVideoCall()
    }

    private fun setupDoctorsList() {
        val doctors = DummyData.getDummyDoctors()
        doctorAdapter = DoctorAdapter(doctors) { doctor ->
            startVideoCall(doctor.id, doctor.name)
        }
        
        binding.rvDoctors.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = doctorAdapter
        }
    }

    private fun setupQuickVideoCall() {
        binding.btnEmergencyVideoCall.setOnClickListener {
            // Emergency call to on-duty doctor
            startVideoCall("doctor_emergency", "Emergency Doctor")
        }
    }

    private fun startVideoCall(doctorId: String, doctorName: String) {
        val patientId = "patient_${System.currentTimeMillis()}"
        val intent = Intent(requireContext(), VideoCallActivity::class.java).apply {
            putExtra("patientId", patientId)
            putExtra("doctorId", doctorId)
            putExtra("doctorName", doctorName)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}