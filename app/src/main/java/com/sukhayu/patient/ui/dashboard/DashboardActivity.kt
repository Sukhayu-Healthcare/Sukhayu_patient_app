package com.sukhayu.patient.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.ai_symptom.CheckSymptomsActivity
import com.sukhayu.patient.ui.ai_symptom.SymptomChatActivity
import com.sukhayu.patient.ui.consultation.ConsultDoctorActivity
import com.sukhayu.patient.ui.awareness.DiseaseOutbreakActivity
import com.sukhayu.patient.ui.consultation.PastConsultationsActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.ui.profile.ProfileActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var consultSymptomLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // ---------------------------
        //  RESULT LAUNCHER
        // ---------------------------
        consultSymptomLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {

                val symptomText =
                    result.data!!.getStringExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_TEXT) ?: ""

                val analysis =
                    result.data!!.getStringExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_ANALYSIS) ?: ""

                val consultIntent = Intent(this, ConsultDoctorActivity::class.java).apply {
                    putExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_TEXT, symptomText)
                    putExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_ANALYSIS, analysis)
                }

                startActivity(consultIntent)
            }
        }

        // ---------------------------
        //  PROFILE
        // ---------------------------
        val cardProfile = findViewById<MaterialCardView>(R.id.cardProfile)
        cardProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("patientId", "P001")
            intent.putExtra("patientName", "Dummy Patient")
            startActivity(intent)
        }

        // ---------------------------
        //  AI SYMPTOM CHECKER
        // ---------------------------
        findViewById<MaterialCardView>(R.id.cardCheckSymptoms).setOnClickListener {
            startActivity(Intent(this, SymptomChatActivity::class.java))
        }

        // ---------------------------
        //  CONSULT DOCTOR (Launches questionnaire)
        // ---------------------------
        findViewById<MaterialCardView>(R.id.cardConsultDoctor).setOnClickListener {
            val intent = Intent(this, CheckSymptomsActivity::class.java)
            consultSymptomLauncher.launch(intent)
        }

        // ---------------------------
        //  PAST CONSULTATIONS
        // ---------------------------
        findViewById<MaterialCardView>(R.id.cardPastConsultations).setOnClickListener {
            startActivity(Intent(this, PastConsultationsActivity::class.java))
        }

        // ---------------------------
        //  DISEASE OUTBREAK
        // ---------------------------
        findViewById<MaterialCardView>(R.id.cardDiseaseOutbreak).setOnClickListener {
            startActivity(Intent(this, DiseaseOutbreakActivity::class.java))
        }

        // ---------------------------
        //  EMERGENCY BUTTON
        // ---------------------------
        findViewById<Button>(R.id.btnEmergency).setOnClickListener {
            startActivity(Intent(this, PastConsultationsActivity::class.java))
        }

        // ---------------------------
        //  LOGOUT BUTTON
        // ---------------------------
        findViewById<Button>(R.id.btnLogout).setOnClickListener {

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
