package com.sukhayu.patient.ui.videocall

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.voicecall.VoiceCallActivity

class VideoCallActivity : AppCompatActivity() {

    private lateinit var btnEndCall: Button
    private var emergencyMode = false
    private var doctorName = ""
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        emergencyMode = intent.getBooleanExtra("EMERGENCY_MODE", false)
        doctorName = intent.getStringExtra("DOCTOR_NAME") ?: "Dr. अमित कुमार"

        initViews()
        setupListeners()
        simulateVideoCallIssue()
    }

    private fun initViews() {
        btnEndCall = findViewById(R.id.btnEndCall)
    }

    private fun setupListeners() {
        btnEndCall.setOnClickListener {
            endCall()
        }
    }

    private fun simulateVideoCallIssue() {
        // Simulate video connection issue after 5 seconds
        handler.postDelayed({
            fallbackToVoiceCall()
        }, 5000)
    }

    private fun fallbackToVoiceCall() {
        Toast.makeText(this, "Video connection unstable. Switching to voice call...", Toast.LENGTH_LONG).show()
        
        val intent = Intent(this, VoiceCallActivity::class.java)
        intent.putExtra("EMERGENCY_MODE", emergencyMode)
        intent.putExtra("DOCTOR_NAME", doctorName)
        startActivity(intent)
        finish()
    }

    private fun endCall() {
        handler.removeCallbacksAndMessages(null)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
