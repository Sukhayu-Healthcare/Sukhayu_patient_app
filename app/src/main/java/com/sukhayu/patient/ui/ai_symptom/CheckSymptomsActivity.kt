package com.sukhayu.patient.ui.ai_symptom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.databinding.ActivitySymptomChatBinding
import com.sukhayu.utils.VoiceInputHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CheckSymptomsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySymptomChatBinding
    private lateinit var voiceHelper: VoiceInputHelper

    companion object {
        const val EXTRA_SYMPTOM_TEXT = "extra_symptom_text"
        const val EXTRA_SYMPTOM_ANALYSIS = "extra_symptom_analysis"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySymptomChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)

        // Back button
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Send button - when user types symptoms and taps send we return the text and an analysis
        binding.btnSend.setOnClickListener {
            val input = binding.etSymptoms.text?.toString()?.trim()
            if (input.isNullOrEmpty()) {
                binding.etSymptoms.error = "Please describe your symptoms"
                return@setOnClickListener
            }

            // Use existing rule-based inference
            val analysis = SymptomRules.inferDisease(input)

            // Optionally show a bot message in the chat UI — quick append (not mandatory)
            // If you use RecyclerView + adapter you can add messages there. For now we return result.

            val resultIntent = Intent().apply {
                putExtra(EXTRA_SYMPTOM_TEXT, input)
                putExtra(EXTRA_SYMPTOM_ANALYSIS, analysis)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish() // return to caller (ConsultDoctorActivity)
        }

        // Optional microphone click (no-op if not implemented)
        binding.btnMic.setOnClickListener {
            // future: start speech-to-text
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
