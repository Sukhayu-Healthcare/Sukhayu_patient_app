package com.sukhayu.patient.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.TtsHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        ttsHelper = TtsHelper(this)
        ttsHelper.setLanguage("en")

        // TODO: Add TTS to your views
        // setupTtsForViews()
    }

    private fun showError(message: String) {
        // ...existing code...
        ttsHelper.speak(message)
    }

    override fun onDestroy() {
        ttsHelper.shutdown()
        super.onDestroy()
    }
}