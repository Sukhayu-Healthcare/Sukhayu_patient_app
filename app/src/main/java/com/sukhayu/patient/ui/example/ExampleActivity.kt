package com.sukhayu.patient.ui.example

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.TtsHelper

class ExampleActivity : AppCompatActivity() {

    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        ttsHelper = TtsHelper(this)
        ttsHelper.setLanguage("en") // or "mr" for Marathi

        setupTtsForViews()
    }

    private fun setupTtsForViews() {
        // TextViews - single click to speak
        findViewById<TextView>(R.id.tvTitle)?.setOnClickListener {
            ttsHelper.speak((it as TextView).text.toString())
        }

        // EditTexts - long press to speak hint
        findViewById<EditText>(R.id.etInput)?.setOnLongClickListener {
            ttsHelper.speak((it as EditText).hint?.toString() ?: "Input field")
            true
        }

        // Buttons - long press to speak (preserves normal click)
        findViewById<Button>(R.id.btnSubmit)?.setOnLongClickListener {
            ttsHelper.speak((it as Button).text.toString())
            true
        }
    }

    private fun showError(message: String) {
        // Show error to user
        // ...existing code...

        // Speak error message
        ttsHelper.speak(message)
    }

    override fun onDestroy() {
        ttsHelper.shutdown()
        super.onDestroy()
    }
}