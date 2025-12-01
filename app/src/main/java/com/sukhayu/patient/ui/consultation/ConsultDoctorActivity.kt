package com.sukhayu.patient.ui.consultation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.ai_symptom.CheckSymptomsActivity
import com.sukhayu.utils.VoiceInputHelper

class ConsultDoctorActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consult_doctor)

        // ------------ Start Questionnaire  ------------
        val startView = findViewById<View>(R.id.cardStart) // replace with your ID
        startView?.setOnClickListener {
            startActivity(Intent(this, CheckSymptomsActivity::class.java))
        }

        // ------------ Video Call Button ------------
        val btnVideoCall = findViewById<Button>(R.id.btnVideoCall)
        if (btnVideoCall == null) {
            android.util.Log.e("ConsultDoctorActivity", "btnVideoCall is NULL!")
        } else {
            android.util.Log.d("ConsultDoctorActivity", "btnVideoCall found!")
            btnVideoCall.setOnClickListener {
                android.util.Log.d("ConsultDoctorActivity", "Video call button clicked!")
                startVideoCall()
            }
        }

        // ------------ Back Button ------------
        val backButton = findViewById<View>(R.id.backButton)
        backButton?.setOnClickListener { finish() }

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

    private fun startVideoCall() {
        val patientId = "patient_${System.currentTimeMillis()}"
        val doctorId = "doctor_consult"

        val intent = Intent(this, VideoCallActivity::class.java).apply {
            putExtra("patientId", patientId)
            putExtra("doctorId", doctorId)
            putExtra("doctorName", "Consultation Doctor")
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
