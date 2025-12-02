package com.sukhayu.patient.ui.emergency

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.videocall.VideoCallActivity

class EmergencyActivity : AppCompatActivity() {

    private lateinit var btnCancel: Button
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnCancel = findViewById(R.id.btnCancel)
        btnConfirm = findViewById(R.id.btnConfirm)
    }

    private fun setupListeners() {
        btnCancel.setOnClickListener {
            finish()
        }

        btnConfirm.setOnClickListener {
            handleEmergencyConfirmation()
        }
    }

    private fun handleEmergencyConfirmation() {
        Toast.makeText(this, "Connecting to emergency doctor...", Toast.LENGTH_SHORT).show()

        // Start video call
        val intent = Intent(this, VideoCallActivity::class.java)
        intent.putExtra("EMERGENCY_MODE", true)
        intent.putExtra("DOCTOR_NAME", "Dr. अमित कुमार")
        startActivity(intent)
        finish()
    }
}
