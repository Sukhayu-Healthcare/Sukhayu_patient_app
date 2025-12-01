package com.sukhayu.patient.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.utils.TtsHelper

class MainActivity : AppCompatActivity() {

    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ...existing code...

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