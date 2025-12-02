package com.sukhayu.patient.ui.voicecall

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.chat.ChatFallbackActivity
import com.sukhayu.patient.ui.videocall.VideoCallActivity

class VoiceCallActivity : AppCompatActivity() {

    private lateinit var tvDoctorName: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvCallStatus: TextView
    private lateinit var btnTurnOnVideo: ImageButton
    private lateinit var btnChat: ImageButton
    private lateinit var btnEndCall: ImageButton

    private var emergencyMode = false
    private var doctorName = ""
    private val handler = Handler(Looper.getMainLooper())
    private var callDuration = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_call)

        emergencyMode = intent.getBooleanExtra("EMERGENCY_MODE", false)
        doctorName = intent.getStringExtra("DOCTOR_NAME") ?: "Dr. अमित कुमार"

        initViews()
        setupListeners()
        startTimer()
        simulateVoiceCallIssue()
    }

    private fun initViews() {
        tvDoctorName = findViewById(R.id.tvDoctorName)
        tvTimer = findViewById(R.id.tvTimer)
        tvCallStatus = findViewById(R.id.tvCallStatus)
        btnTurnOnVideo = findViewById(R.id.btnTurnOnVideo)
        btnChat = findViewById(R.id.btnChat)
        btnEndCall = findViewById(R.id.btnEndCall)

        tvDoctorName.text = doctorName
    }

    private fun setupListeners() {
        btnTurnOnVideo.setOnClickListener {
            switchToVideo()
        }

        btnChat.setOnClickListener {
            switchToChat()
        }

        btnEndCall.setOnClickListener {
            endCall()
        }
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                callDuration++
                val minutes = callDuration / 60
                val seconds = callDuration % 60
                tvTimer.text = String.format("%d:%02d", minutes, seconds)
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun simulateVoiceCallIssue() {
        // Simulate voice connection issue after 8 seconds
        handler.postDelayed({
            fallbackToChat()
        }, 8000)
    }

    private fun switchToVideo() {
        handler.removeCallbacksAndMessages(null)
        val intent = Intent(this, VideoCallActivity::class.java)
        intent.putExtra("EMERGENCY_MODE", emergencyMode)
        intent.putExtra("DOCTOR_NAME", doctorName)
        startActivity(intent)
        finish()
    }

    private fun switchToChat() {
        handler.removeCallbacksAndMessages(null)
        val intent = Intent(this, ChatFallbackActivity::class.java)
        intent.putExtra("EMERGENCY_MODE", emergencyMode)
        intent.putExtra("DOCTOR_NAME", doctorName)
        startActivity(intent)
        finish()
    }

    private fun fallbackToChat() {
        Toast.makeText(this, "Voice connection unstable. Switching to chat...", Toast.LENGTH_LONG).show()
        switchToChat()
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
