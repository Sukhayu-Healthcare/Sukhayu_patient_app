package com.sukhayu.patient.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.TtsHelper

class AppointmentsFragment : Fragment() {

    private lateinit var ttsHelper: TtsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_appointments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ttsHelper = TtsHelper(requireContext())
        ttsHelper.setLanguage("en")

        setupTtsForViews(view)
    }

    private fun setupTtsForViews(view: View) {
        view.findViewById<TextView>(R.id.textViewAppointmentsTitle)?.setOnClickListener {
            ttsHelper.speak((it as TextView).text.toString())
        }

        view.findViewById<TextView>(R.id.textViewNoAppointments)?.setOnClickListener {
            ttsHelper.speak((it as TextView).text.toString())
        }

        view.findViewById<Button>(R.id.buttonNewAppointment)?.setOnLongClickListener {
            ttsHelper.speak((it as Button).text.toString())
            true
        }
    }

    override fun onDestroyView() {
        ttsHelper.shutdown()
        super.onDestroyView()
    }
}