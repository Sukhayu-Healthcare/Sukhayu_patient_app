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
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.ui.asha.nhp.NationalHealthProgramsActivity
import com.sukhayu.patient.ui.asha.registration.RegisterPatientActivity
import com.sukhayu.patient.asha.ui.surveys.AshaSurveyHomeActivity
import com.sukhayu.patient.ui.asha.family.FamilyListActivity
import com.sukhayu.patient.ui.asha.emergency.EmergencyContactsActivity
import com.sukhayu.patient.ui.asha.schedule.AshaScheduleActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.utils.VoiceInputHelper

class AshaDashboardActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_dashboard)

        // Load and display ASHA profile data
        loadProfileData()



        // Make Total Patients card clickable
        findViewById<androidx.cardview.widget.CardView>(R.id.cardTotalPatients).setOnClickListener {
            startActivity(Intent(this, FamilyListActivity::class.java))
        }

        // Make Emergency card clickable
        findViewById<androidx.cardview.widget.CardView>(R.id.cardEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyContactsActivity::class.java))
        }

        // Make My Schedule card clickable
        findViewById<androidx.cardview.widget.CardView>(R.id.cardMySchedule).setOnClickListener {
            startActivity(Intent(this, AshaScheduleActivity::class.java))
        }

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


        // Logout button
        findViewById<Button>(R.id.tv_logout).setOnClickListener {
            logout()
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

    private fun loadProfileData() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val ashaName = prefs.getString("user_name", "ASHA Worker") ?: "ASHA Worker"
        val ashaId = prefs.getString("user_id", "N/A") ?: "N/A"

        // Set default values first
        findViewById<TextView>(R.id.tv_asha_name).text = ashaName
        findViewById<TextView>(R.id.tvAshaId).text = "ID: $ashaId"
        findViewById<TextView>(R.id.tv_asha_village).text = "Village: -"
        findViewById<TextView>(R.id.tv_asha_taluka).text = "Taluka: -"
        findViewById<TextView>(R.id.tv_asha_district).text = "District: -"

        // Fetch complete profile from API
        val token = TokenManager.getToken()
        if (token.isNotEmpty()) {
            ApiClient.retrofit.getSupervisorProfile("Bearer $token")
                .enqueue(object : retrofit2.Callback<com.sukhayu.patient.data.remote.SupervisorProfile> {
                    override fun onResponse(
                        call: retrofit2.Call<com.sukhayu.patient.data.remote.SupervisorProfile>,
                        response: retrofit2.Response<com.sukhayu.patient.data.remote.SupervisorProfile>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val profile = response.body()!!

                            findViewById<TextView>(R.id.tv_asha_name).text = profile.user_name ?: ashaName
                            findViewById<TextView>(R.id.tvAshaId).text = "ID: ${profile.asha_id ?: ashaId}"
                            findViewById<TextView>(R.id.tv_asha_village).text = "Village: ${profile.village ?: "-"}"
                            findViewById<TextView>(R.id.tv_asha_taluka).text = "Taluka: ${profile.taluka ?: "-"}"
                            findViewById<TextView>(R.id.tv_asha_district).text = "District: ${profile.district ?: "-"}"
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.sukhayu.patient.data.remote.SupervisorProfile>,
                        t: Throwable
                    ) {
                        Log.e("AshaDashboard", "Failed to load profile: ${t.message}")
                        // Keep default values
                    }
                })
        }
    }

    private fun logout() {
        // Clear shared preferences
        getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()

        // Clear TokenManager
        TokenManager.clearToken()

        // Navigate to login with clear task flag
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
