package com.sukhayu.patient.ui.emergency

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R

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
        // Verify patient is logged in before starting emergency call
        val patientId = com.sukhayu.patient.utils.TokenManager.getSupremeId().ifEmpty {
            com.sukhayu.patient.utils.TokenManager.getUserId()
        }

        if (patientId.isEmpty()) {
            Toast.makeText(this, "Please login to use emergency services", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Toast.makeText(this, "Connecting to emergency doctor...", Toast.LENGTH_SHORT).show()

        // Start emergency video call with WebRTC
        val intent = Intent(this, EmergencyVCActivity::class.java)
        intent.putExtra("PREFERRED_LEVEL", "MO") // or "CHO", "CIVIL"
        startActivity(intent)
        finish()
    }
}
