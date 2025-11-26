package com.sukhayu.patient.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.card.MaterialCardView
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.ai_symptom.CheckSymptomsActivity
import com.sukhayu.patient.ui.consultation.ConsultDoctorActivity
import com.sukhayu.patient.ui.ai_symptom.SymptomChatActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.ui.consultation.PastConsultationsActivity
import com.sukhayu.patient.ui.consultation.MedicinesActivity
import com.sukhayu.patient.ui.profile.ProfileActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var consultSymptomLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // initialize ActivityResult launcher for launching the symptom questionnaire
        consultSymptomLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val symptomText = result.data!!.getStringExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_TEXT) ?: ""
                val analysis = result.data!!.getStringExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_ANALYSIS) ?: ""

                // Start ConsultDoctorActivity and pass the symptom text + analysis so it auto-allocates
                val consultIntent = Intent(this, ConsultDoctorActivity::class.java).apply {
                    putExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_TEXT, symptomText)
                    putExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_ANALYSIS, analysis)
                }
                startActivity(consultIntent)
            }
        }

        // ---------------------------
        //  PROFILE CARD
        // ---------------------------
        val cardProfile = findViewById<MaterialCardView>(R.id.cardProfile)
        cardProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("patientId", "P001") // sample id, change if dynamic
            intent.putExtra("patientName", "Dummy Patient")
            startActivity(intent)
        }

        // ---------------------------
        //  AI SYMPTOM CHECKER
        // ---------------------------
        val cardCheckSymptoms = findViewById<MaterialCardView>(R.id.cardCheckSymptoms)
        cardCheckSymptoms.setOnClickListener {
            startActivity(Intent(this, SymptomChatActivity::class.java))
        }

        // ---------------------------
        //  CONSULT DOCTOR (now launches questionnaire directly)
        // ---------------------------
        val cardConsultDoctor = findViewById<MaterialCardView>(R.id.cardConsultDoctor)
        cardConsultDoctor.setOnClickListener {
            // Launch the questionnaire UI directly. When it returns, launcher callback will open ConsultDoctorActivity.
            val intent = Intent(this, CheckSymptomsActivity::class.java)
            consultSymptomLauncher.launch(intent)
        }

        // ---------------------------
        //  PAST CONSULTATIONS
        // ---------------------------
        val cardPast = findViewById<MaterialCardView>(R.id.cardPastConsultations)
        cardPast.setOnClickListener {
            startActivity(Intent(this, PastConsultationsActivity::class.java))
        }

        // ---------------------------
        //  MEDICINES
        // ---------------------------
        val cardMedicines = findViewById<MaterialCardView>(R.id.cardMedicines)
        cardMedicines.setOnClickListener {
            startActivity(Intent(this, MedicinesActivity::class.java))
        }

        // ---------------------------
        //  EMERGENCY BUTTON
        // ---------------------------
        val btnEmergency = findViewById<Button>(R.id.btnEmergency)
        btnEmergency.setOnClickListener {
            // You can show a dialog or open emergency activity
            // For now launching PastConsultationsActivity just as placeholder
            startActivity(Intent(this, PastConsultationsActivity::class.java))
        }

        // ---------------------------
        //  LOGOUT BUTTON
        // ---------------------------
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            getSharedPreferences("auth", MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
