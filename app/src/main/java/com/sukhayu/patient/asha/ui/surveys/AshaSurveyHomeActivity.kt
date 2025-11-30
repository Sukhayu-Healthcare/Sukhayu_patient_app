package com.sukhayu.patient.asha.ui.surveys

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.sukhayu.patient.R
import com.sukhayu.patient.asha.ui.surveys.child.ChildSurveyActivity
import com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity
import com.sukhayu.patient.asha.ui.surveys.pregnancy.PregnancySurveyActivity
import com.sukhayu.patient.asha.ui.surveys.tb.TbSurveyActivity
import com.sukhayu.patient.ui.asha.search.PatientSearchForGeneralSurveyActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.utils.VoiceInputHelper

class AshaSurveyHomeActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AshaSurveyHome", "Activity onCreate called")
        setContentView(R.layout.activity_asha_survey_home)

        // Setup header
        setupHeader()

        // Load profile data
        loadProfileData()

        // Setup logout button
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }

        // Wire survey button clicks
        findViewById<MaterialCardView>(R.id.btnPregnancySurvey).setOnClickListener {
            startActivity(Intent(this, PregnancySurveyActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.btnChildSurvey).setOnClickListener {
            startActivity(Intent(this, ChildSurveyActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.btnTbSurvey).setOnClickListener {
            startActivity(Intent(this, TbSurveyActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.btnGeneralSurvey).setOnClickListener {
            // Navigate to patient search first, then to General Survey form
            startActivity(Intent(this, PatientSearchForGeneralSurveyActivity::class.java))
        }

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun setupHeader() {
        findViewById<ImageView>(R.id.btnBack)?.apply {
            visibility = android.view.View.VISIBLE
            setOnClickListener { finish() }
        }
        findViewById<TextView>(R.id.headerTitle)?.text = "Surveys"
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val ashaName = prefs.getString("user_name", "ASHA Worker") ?: "ASHA Worker"
        val ashaId = prefs.getString("user_id", "N/A") ?: "N/A"

        findViewById<TextView>(R.id.tvAshaName).text = ashaName
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
