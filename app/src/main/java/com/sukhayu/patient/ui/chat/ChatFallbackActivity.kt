package com.sukhayu.patient.ui.chat

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.dashboard.DashboardActivity
import com.sukhayu.patient.ui.teleconsult.VideoCallActivity
import com.sukhayu.patient.ui.teleconsult.VoiceCallActivity

class ChatFallbackActivity : AppCompatActivity() {

    private lateinit var tvDoctorName: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnVoiceCall: ImageButton
    private lateinit var btnVideoCall: ImageButton
    private lateinit var btnMore: ImageButton
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton

    private var emergencyMode = false
    private var doctorName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_fallback)

        emergencyMode = intent.getBooleanExtra("EMERGENCY_MODE", false)
        doctorName = intent.getStringExtra("DOCTOR_NAME") ?: "Dr. अमित कुमार"

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tvDoctorName = findViewById(R.id.tvDoctorName)
        btnBack = findViewById(R.id.btnBack)
        btnVoiceCall = findViewById(R.id.btnVoiceCall)
        btnVideoCall = findViewById(R.id.btnVideoCall)
        btnMore = findViewById(R.id.btnMore)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        tvDoctorName.text = doctorName
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            returnToHomepage()
        }

        btnVoiceCall.setOnClickListener {
            startVoiceCall()
        }

        btnVideoCall.setOnClickListener {
            startVideoCall()
        }

        btnSend.setOnClickListener {
            sendMessage()
        }

        btnMore.setOnClickListener {
            showMoreOptions()
        }
    }

    private fun sendMessage() {
        val message = etMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            // TODO: Implement actual message sending
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
            etMessage.setText("")
        }
    }

    private fun startVoiceCall() {
        val intent = Intent(this, VoiceCallActivity::class.java)
        intent.putExtra("EMERGENCY_MODE", emergencyMode)
        intent.putExtra("DOCTOR_NAME", doctorName)
        startActivity(intent)
    }

    private fun startVideoCall() {
        val intent = Intent(this, VideoCallActivity::class.java)
        intent.putExtra("EMERGENCY_MODE", emergencyMode)
        intent.putExtra("DOCTOR_NAME", doctorName)
        startActivity(intent)
    }

    private fun showMoreOptions() {
        Toast.makeText(this, "Ending consultation...", Toast.LENGTH_SHORT).show()
        returnToHomepage()
    }

    private fun returnToHomepage() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        returnToHomepage()
    }
}
