package com.sukhayu.patient.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.ai_symptom.SymptomChatActivity
import com.sukhayu.patient.ui.consultation.ConsultDoctorActivity
import com.sukhayu.patient.ui.consultation.PastConsultationsActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.ui.profile.ProfileActivity
import com.sukhayu.patient.utils.HeaderUtils
import com.sukhayu.utils.LocaleHelper
import com.sukhayu.patient.ui.patient.appointment.BookAppointmentActivity
import com.sukhayu.patient.ui.emergency.EmergencyActivity
import com.sukhayu.patient.ui.ai_symptom.CheckSymptomsActivity
import com.sukhayu.patient.ui.patient.query.PatientQueryActivity
import com.sukhayu.utils.VoiceInputHelper
import com.sukhayu.patient.utils.LocalizableActivity
import android.view.View
import android.widget.AdapterView
import com.sukhayu.patient.utils.TtsHelper
import com.sukhayu.patient.utils.ViewTtsHelper

class DashboardActivity : LocalizableActivity(){

    private lateinit var consultSymptomLauncher: ActivityResultLauncher<Intent>
    private lateinit var voiceHelper: VoiceInputHelper

    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Set role label in header
        HeaderUtils.setupRoleInHeader(this)
        setupLanguageToggle()

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userName = prefs.getString("user_name", "") ?: ""
        val userPhone = prefs.getString("user_phone", "") ?: ""

        findViewById<TextView>(R.id.tvUserName)?.text = userName
        findViewById<TextView>(R.id.tvUserPhone)?.text = userPhone

        // RESULT HANDLING for symptom checker → consult doctor
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

        // PROFILE CARD
        findViewById<MaterialCardView>(R.id.cardProfile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("patientId", prefs.getString("user_id", ""))
                putExtra("patientName", prefs.getString("user_name", ""))
            }
            startActivity(intent)
        }

        // AI SYMPTOMS
        findViewById<MaterialCardView>(R.id.cardCheckSymptoms).setOnClickListener {
            startActivity(Intent(this, SymptomChatActivity::class.java))
        }

        // WE CARE - HEALTH QUERIES
        findViewById<MaterialCardView>(R.id.cardWecare).setOnClickListener {
            Log.d("DashboardActivity", "We Care button clicked - opening PatientQueryActivity")
            startActivity(Intent(this, PatientQueryActivity::class.java))
        }

        // BOOK APPOINTMENT
        findViewById<MaterialCardView>(R.id.cardBookAppointment).setOnClickListener {
            startActivity(Intent(this, BookAppointmentActivity::class.java))
        }

        // PAST CONSULTATIONS
        findViewById<MaterialCardView>(R.id.cardPastConsultations).setOnClickListener {
            startActivity(Intent(this, PastConsultationsActivity::class.java))
        }

        // EMERGENCY
        findViewById<Button>(R.id.btnEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }

        // WEBRTC CALL BUTTON
        findViewById<Button>(R.id.btnWebRTCCall)?.setOnClickListener {
            val patientId = prefs.getString("user_id", "") ?: ("patient_" + System.currentTimeMillis())
            val intent = Intent(this, com.sukhayu.patient.ui.teleconsult.VideoCallActivity::class.java)
            intent.putExtra("patientId", patientId)
            startActivity(intent)
        }

        // LOGOUT
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)

        // Initialize TTS
        ttsHelper = TtsHelper(this)

        val currentLang = prefs.getString("My_Lang", "en") ?: "en"

        ttsHelper.setLanguage(currentLang)

        // Enable TTS on all TextViews and Buttons
        ViewTtsHelper.attachToAllTextViews(
            findViewById(android.R.id.content),
            ttsHelper
        )
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
