package com.sukhayu.patient.ui.ai_symptom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.teleconsult.ConsentActivity
import com.sukhayu.patient.ui.consultation.VideoCallActivity
import com.sukhayu.patient.utils.TtsHelper

class AiSymptomFragment : Fragment() {
    
    private lateinit var ttsHelper: TtsHelper

    private val consentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = Intent(requireContext(), VideoCallActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

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

        // Wire the Send button to open consent flow
        val btnSend = view.findViewById<Button>(R.id.btnSend)
        btnSend.setOnClickListener {
            val intent = Intent(requireContext(), ConsentActivity::class.java)
            consentLauncher.launch(intent)
        }

        // TODO: Setup chat adapter and recycler view
    }

    override fun onDestroyView() {
        ttsHelper.shutdown()
        super.onDestroyView()
    }
}