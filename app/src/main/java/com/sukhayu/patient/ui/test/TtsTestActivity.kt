package com.sukhayu.patient.ui.test

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.TtsHelper

class TtsTestActivity : AppCompatActivity() {

    private lateinit var ttsHelper: TtsHelper
    private lateinit var etTestText: EditText
    private lateinit var btnSpeak: Button
    private lateinit var btnStop: Button
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tts_test)

        etTestText = findViewById(R.id.etTestText)
        btnSpeak = findViewById(R.id.btnSpeak)
        btnStop = findViewById(R.id.btnStop)
        tvStatus = findViewById(R.id.tvStatus)

        // Initialize TTS
        ttsHelper = TtsHelper(this)
        ttsHelper.setLanguage("en")
        
        tvStatus.text = "TTS Initialized. Ready to speak."

        // Speak button
        btnSpeak.setOnClickListener {
            val text = etTestText.text.toString().trim()
            if (text.isNotEmpty()) {
                tvStatus.text = "Speaking: $text"
                ttsHelper.speak(text)
            } else {
                tvStatus.text = "Please enter some text"
            }
        }

        // Stop button
        btnStop.setOnClickListener {
            ttsHelper.stop()
            tvStatus.text = "Speech stopped"
        }

        // Long press on TextView to speak its content
        tvStatus.setOnLongClickListener {
            ttsHelper.speak(tvStatus.text.toString())
            true
        }

        // Pre-fill some test text
        etTestText.setText("Hello, this is a test of the Text to Speech system.")
    }

    override fun onDestroy() {
        ttsHelper.shutdown()
        super.onDestroy()
    }
}
