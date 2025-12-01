package com.sukhayu.patient.ui.ai_symptom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.TtsHelper

class AiSymptomFragment : Fragment() {
    
    private lateinit var ttsHelper: TtsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_ai_symptom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsHelper = TtsHelper(requireContext())
        ttsHelper.setLanguage("en")

        // TODO: Setup chat adapter and recycler view
    }

    override fun onDestroyView() {
        ttsHelper.shutdown()
        super.onDestroyView()
    }
}