package com.sukhayu.patient.ui.supervisor.drives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sukhayu.patient.databinding.FragmentDrivesBinding
import com.sukhayu.patient.utils.TtsHelper

class DrivesFragment : Fragment() {

    private var _binding: FragmentDrivesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var ttsHelper: TtsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrivesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsHelper = TtsHelper(requireContext())
        ttsHelper.setLanguage("en")

        // TODO: Pass ttsHelper to DriveAdapter
        // val adapter = DriveAdapter(driveItems, ttsHelper)
        // binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        ttsHelper.shutdown()
        _binding = null
        super.onDestroyView()
    }
}