package com.sukhayu.patient.ui.ai_symptom

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.databinding.ActivitySymptomChatBinding

class CheckSymptomsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySymptomChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ViewBinding instead of findViewById
        binding = ActivitySymptomChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back button handler
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
