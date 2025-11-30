package com.sukhayu.patient.asha.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.utils.TokenManager

class AshaDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AshaDashboard", "Activity onCreate called")
        setContentView(R.layout.activity_asha_dashboard)

        // Setup header
        setupHeader()

        // Load profile data
        loadProfileData()

        // Setup logout button
        findViewById<Button>(R.id.tv_logout).setOnClickListener {
            logout()
        }

        // Wire survey button clicks
        findViewById<Button>(R.id.btnSurveys).setOnClickListener {
            startActivity(Intent(this, com.sukhayu.patient.asha.ui.surveys.AshaSurveyHomeActivity::class.java))
        }

        // ...other button click listeners as needed
    }

    private fun setupHeader() {
        findViewById<ImageView>(R.id.btnBack)?.visibility = android.view.View.GONE
        findViewById<TextView>(R.id.headerTitle)?.text = "ASHA Dashboard"
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val ashaName = prefs.getString("user_name", "ASHA Worker") ?: "ASHA Worker"
        val ashaId = prefs.getString("user_id", "A001") ?: "A001"

        findViewById<TextView>(R.id.tv_asha_name).text = ashaName
        findViewById<TextView>(R.id.tvAshaId).text = "ID: $ashaId"
    }

    private fun logout() {
        // Clear shared preferences
        getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()

        // Clear TokenManager
        TokenManager.clearToken()

        // Navigate to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}