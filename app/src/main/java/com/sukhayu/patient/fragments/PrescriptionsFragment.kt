package com.sukhayu.patient.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.TtsHelper

class PrescriptionsFragment : Fragment() {

    private lateinit var ttsHelper: TtsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_prescriptions, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsHelper = TtsHelper(requireContext())
        ttsHelper.setLanguage("en")

        setupTtsForViews(view)
    }

    private fun setupTtsForViews(view: View) {
        view.findViewById<TextView>(R.id.textViewPrescriptionsTitle)?.setOnClickListener {
            ttsHelper.speak((it as TextView).text.toString())
        }

        view.findViewById<TextView>(R.id.textViewNoPrescriptions)?.setOnClickListener {
            ttsHelper.speak((it as TextView).text.toString())
        }
    }

    override fun onDestroyView() {
        ttsHelper.shutdown()
        super.onDestroyView()
    }
}