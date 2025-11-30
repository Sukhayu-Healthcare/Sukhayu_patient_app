package com.sukhayu.patient.ui.asha.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.asha.nhp.NationalHealthProgramsActivity
import com.sukhayu.patient.ui.asha.registration.RegisterPatientActivity
import com.sukhayu.patient.asha.ui.surveys.AshaSurveyHomeActivity
import com.sukhayu.utils.VoiceInputHelper

class AshaDashboardActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_dashboard)

        // header ASHA name
        findViewById<TextView>(R.id.tv_asha_name).text = getString(R.string.sample_asha_name)

        // static metrics (replace with DB values later)
        findViewById<TextView>(R.id.tv_total_patients).text = "12"
        findViewById<TextView>(R.id.tv_active_cases).text = "7"
        findViewById<TextView>(R.id.tv_completed_today).text = "0"
        findViewById<TextView>(R.id.tv_emergency_cases).text = "2"

        // Quick action button wiring using direct class Intents (compile-safe)
        findViewById<Button>(R.id.btn_view_surveys).setOnClickListener {
            // TODO: Open view/list of past surveys or keep as NHP
            startActivity(Intent(this, NationalHealthProgramsActivity::class.java))
        }

        // Conduct Survey button navigates to Survey Home
        findViewById<Button>(R.id.btnSurveys).setOnClickListener {
            Log.d("AshaDashboard", "Conduct Survey button clicked")
            try {
                val intent = Intent(this, AshaSurveyHomeActivity::class.java)
                startActivity(intent)
                Log.d("AshaDashboard", "Survey Home Activity started successfully")
            } catch (e: Exception) {
                Log.e("AshaDashboard", "Error starting survey activity: ${e.message}")
                Toast.makeText(this, "Error opening surveys: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_health_drives).setOnClickListener {
            startActivity(Intent(this, NationalHealthProgramsActivity::class.java))
        }

        findViewById<Button>(R.id.btn_register_patient).setOnClickListener {
            startActivity(Intent(this, RegisterPatientActivity::class.java))
        }

        findViewById<Button>(R.id.btn_help_login).setOnClickListener {
            // TODO: show help dialog/screen
        }

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

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
