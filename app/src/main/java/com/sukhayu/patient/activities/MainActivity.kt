package com.sukhayu.patient.activities

import android.os.Bundle
import com.sukhayu.patient.utils.LocalizableActivity
import com.sukhayu.patient.utils.TtsHelper

class MainActivity : LocalizableActivity() {

    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ...existing code...

        // Setup language toggle in header
        setupLanguageToggle()

        // Initialize TTS
        ttsHelper = TtsHelper(this)
        ttsHelper.setLanguage("en")

        // TODO: Add TTS to your views in setupTtsForViews()
        // setupTtsForViews()
    }

    // ...existing code...

    override fun onDestroy() {
        ttsHelper.shutdown()
        super.onDestroy()
    }
}