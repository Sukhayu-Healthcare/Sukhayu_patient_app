package com.sukhayu.patient.ui.supervisor.surveys

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sukhayu.patient.databinding.FragmentSurveysBinding
import com.sukhayu.patient.utils.TtsHelper

class SurveysFragment : Fragment() {

    private var _binding: FragmentSurveysBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var ttsHelper: TtsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSurveysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsHelper = TtsHelper(requireContext())
        ttsHelper.setLanguage("en")

        // TODO: Pass ttsHelper to SurveyAdapter
        // val adapter = SurveyAdapter(surveyItems, ttsHelper)
    }

    override fun onDestroyView() {
        ttsHelper.shutdown()
        _binding = null
        super.onDestroyView()
    }
}