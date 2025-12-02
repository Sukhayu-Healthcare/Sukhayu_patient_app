package com.sukhayu.patient.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.ai_symptom.CheckSymptomsActivity
import com.sukhayu.patient.ui.ai_symptom.SymptomChatActivity
import com.sukhayu.patient.ui.consultation.ConsultDoctorActivity
import com.sukhayu.patient.ui.awareness.DiseaseOutbreakActivity
import com.sukhayu.patient.ui.consultation.PastConsultationsActivity
import com.sukhayu.patient.ui.emergency.EmergencyActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.ui.profile.ProfileActivity
import com.sukhayu.utils.VoiceInputHelper

class DashboardActivity : AppCompatActivity() {

    private lateinit var consultSymptomLauncher: ActivityResultLauncher<Intent>
    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // ---------------------------------------------------------
        // FETCH USER DETAILS FROM SHARED PREFS
        // ---------------------------------------------------------
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)

        val userName = prefs.getString("user_name", "") ?: ""
        val userPhone = prefs.getString("user_phone", "") ?: ""
        val role = prefs.getString("role", "") ?: ""

        // ---------------------------------------------------------
        // SET VALUES INTO PROFILE CARD
        // ---------------------------------------------------------
        findViewById<TextView>(R.id.tvUserName)?.text = userName
        findViewById<TextView>(R.id.tvUserPhone)?.text = userPhone


        // ---------------------------------------------------------
        //  RESULT LAUNCHER (For AI Symptom → Consult Doctor)
        // ---------------------------------------------------------
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


        // ---------------------------------------------------------
        //  PROFILE CARD CLICK → OPEN PROFILE PAGE
        // ---------------------------------------------------------
        val cardProfile = findViewById<MaterialCardView>(R.id.cardProfile)
        cardProfile.setOnClickListener {

            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("patientId", prefs.getString("user_id", ""))
                putExtra("patientName", prefs.getString("user_name", ""))
            }

            startActivity(intent)
        }


        // ---------------------------------------------------------
        //  AI SYMPTOM CHECKER
        // ---------------------------------------------------------
        findViewById<MaterialCardView>(R.id.cardCheckSymptoms).setOnClickListener {
            startActivity(Intent(this, SymptomChatActivity::class.java))
        }

        // ---------------------------------------------------------
        //  CONSULT DOCTOR → ASK QUESTIONS FIRST
        // ---------------------------------------------------------
        findViewById<MaterialCardView>(R.id.cardConsultDoctor).setOnClickListener {
            val intent = Intent(this, CheckSymptomsActivity::class.java)
            consultSymptomLauncher.launch(intent)
        }

        // ---------------------------------------------------------
        //  PAST CONSULTATIONS
        // ---------------------------------------------------------
        findViewById<MaterialCardView>(R.id.cardPastConsultations).setOnClickListener {
            startActivity(Intent(this, PastConsultationsActivity::class.java))
        }

        // ---------------------------------------------------------
        //  DISEASE OUTBREAK AWARENESS
        // ---------------------------------------------------------
        findViewById<MaterialCardView>(R.id.cardDiseaseOutbreak).setOnClickListener {
            startActivity(Intent(this, DiseaseOutbreakActivity::class.java))
        }

        // ---------------------------------------------------------
        //  EMERGENCY
        // ---------------------------------------------------------
        findViewById<Button>(R.id.btnEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }

        // ---------------------------------------------------------
        //  LOGOUT
        // ---------------------------------------------------------
        findViewById<Button>(R.id.btnLogout).setOnClickListener {

            prefs.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
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
