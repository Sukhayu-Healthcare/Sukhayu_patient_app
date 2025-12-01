package com.sukhayu.patient.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sukhayu.patient.databinding.FragmentProfileBinding
import com.sukhayu.patient.utils.TtsHelper

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var ttsHelper: TtsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsHelper = TtsHelper(requireContext())
        ttsHelper.setLanguage("en")

        // TODO: Add TTS to profile fields
    }

    override fun onDestroyView() {
        ttsHelper.shutdown()
        _binding = null
        super.onDestroyView()
    }
}