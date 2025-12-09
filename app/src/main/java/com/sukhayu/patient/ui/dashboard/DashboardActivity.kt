package com.sukhayu.patient.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
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
import com.sukhayu.patient.ui.ai_symptom.CheckSymptomsActivity
import com.sukhayu.patient.ui.ai_symptom.SymptomChatActivity
import com.sukhayu.patient.ui.awareness.DiseaseOutbreakActivity
import com.sukhayu.patient.ui.consultation.ConsultDoctorActivity
import com.sukhayu.patient.ui.consultation.PastConsultationsActivity
import com.sukhayu.patient.ui.consultation.VideoCallActivity
import com.sukhayu.patient.ui.emergency.EmergencyActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.ui.patient.appointment.BookAppointmentActivity
import com.sukhayu.patient.ui.profile.ProfileActivity
import com.sukhayu.patient.utils.HeaderUtils
import com.sukhayu.utils.LocaleHelper
import com.sukhayu.utils.VoiceInputHelper

class DashboardActivity : AppCompatActivity() {

    private lateinit var consultSymptomLauncher: ActivityResultLauncher<Intent>
    private lateinit var voiceHelper: VoiceInputHelper

    // ----------------------
    // APPLY SAVED LOCALE HERE
    // ----------------------
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_lang", "en") ?: "en"
        val ctx = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(ctx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Set role label in header
        HeaderUtils.setupRoleInHeader(this)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userName = prefs.getString("user_name", "") ?: ""
        val userPhone = prefs.getString("user_phone", "") ?: ""

        findViewById<TextView>(R.id.tvUserName)?.text = userName
        findViewById<TextView>(R.id.tvUserPhone)?.text = userPhone

        // DEBUG: direct video call test button (only active when app is debuggable)
        findViewById<Button?>(R.id.btnDebugTestVideoCall)?.setOnClickListener {
            val isDebuggable =
                (applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (!isDebuggable) {
                Log.w("DashboardActivity", "Debug test button pressed in non-debuggable app; ignoring.")
                return@setOnClickListener
            }

            val mockPatientId = "debug_patient"
            val mockDoctorId = "debug_doctor"
            Log.d(
                "DashboardActivity",
                "Starting debug VideoCallActivity with patient=$mockPatientId doctor=$mockDoctorId"
            )

            val intent = Intent(this, VideoCallActivity::class.java).apply {
                putExtra("patientId", mockPatientId)
                putExtra("doctorId", mockDoctorId)
            }
            startActivity(intent)
        }

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

        // BOOK APPOINTMENT
        findViewById<MaterialCardView>(R.id.cardBookAppointment).setOnClickListener {
            startActivity(Intent(this, BookAppointmentActivity::class.java))
        }

        // PAST CONSULTATIONS
        findViewById<MaterialCardView>(R.id.cardPastConsultations).setOnClickListener {
            startActivity(Intent(this, PastConsultationsActivity::class.java))
        }

        // DISEASE OUTBREAK
        findViewById<MaterialCardView>(R.id.cardDiseaseOutbreak).setOnClickListener {
            startActivity(Intent(this, DiseaseOutbreakActivity::class.java))
        }

        // EMERGENCY
        findViewById<Button>(R.id.btnEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
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
