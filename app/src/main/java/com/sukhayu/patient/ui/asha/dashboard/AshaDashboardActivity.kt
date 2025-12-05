package com.sukhayu.patient.ui.asha.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.asha.ui.surveys.AshaSurveyHomeActivity
import com.sukhayu.patient.asha.ui.surveys.tb.TbScreeningViewModel
import com.sukhayu.patient.ui.asha.emergency.EmergencyContactsActivity
import com.sukhayu.patient.ui.asha.family.FamilyListActivity
import com.sukhayu.patient.ui.asha.nhp.NationalHealthProgramsActivity
import com.sukhayu.patient.ui.asha.registration.RegisterPatientActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.utils.LocaleHelper
import com.sukhayu.utils.VoiceInputHelper

class AshaDashboardActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper

    // TB sync ViewModel
    private lateinit var tbScreeningViewModel: TbScreeningViewModel

    // Apply saved locale before activity context is used
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_lang", "en") ?: "en"
        val wrapped = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(wrapped)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_dashboard)

        tbScreeningViewModel =
            ViewModelProvider(this)[TbScreeningViewModel::class.java]

        loadProfileData()

        findViewById<androidx.cardview.widget.CardView>(R.id.cardTotalPatients).setOnClickListener {
            startActivity(Intent(this, FamilyListActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyContactsActivity::class.java))
        }

        findViewById<Button>(R.id.btn_view_surveys).setOnClickListener {
            startActivity(Intent(this, NationalHealthProgramsActivity::class.java))
        }

        findViewById<Button>(R.id.btnSurveys).setOnClickListener {
            try {
                val intent = Intent(this, AshaSurveyHomeActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error opening surveys: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        findViewById<Button>(R.id.btn_health_drives).setOnClickListener {
            startActivity(Intent(this, NationalHealthProgramsActivity::class.java))
        }

        findViewById<Button>(R.id.btn_register_patient).setOnClickListener {
            startActivity(Intent(this, RegisterPatientActivity::class.java))
        }

        findViewById<Button>(R.id.tv_logout).setOnClickListener {
            logout()
        }

        val tvEnglishId = resources.getIdentifier("tvEnglish", "id", packageName)
        if (tvEnglishId != 0) {
            findViewById<TextView>(tvEnglishId)?.setOnClickListener {
                val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                prefs.edit().putString("app_lang", "en").apply()
                recreate()
            }
        }

        val tvMarathiId = resources.getIdentifier("tvMarathi", "id", packageName)
        if (tvMarathiId != 0) {
            findViewById<TextView>(tvMarathiId)?.setOnClickListener {
                val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                prefs.edit().putString("app_lang", "mr").apply()
                recreate()
            }
        }

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    override fun onResume() {
        super.onResume()

        // Whenever dashboard comes to foreground and network is available → sync TB screenings
        if (isNetworkAvailable()) {
            Log.d("AshaDashboard", "onResume: Network available, syncing TB screenings")
            tbScreeningViewModel.syncPendingTbScreenings { count ->
                Log.d("AshaDashboard", "TB sync finished. Synced count = $count")
            }
        } else {
            Log.d("AshaDashboard", "onResume: No internet. TB sync skipped.")
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
        }
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val ashaName = prefs.getString("user_name", "ASHA Worker") ?: "ASHA Worker"
        val ashaId = prefs.getString("user_id", "N/A") ?: "N/A"

        findViewById<TextView>(R.id.tv_asha_name).text = ashaName
        findViewById<TextView>(R.id.tvAshaId).text = "ID: $ashaId"
        findViewById<TextView>(R.id.tv_asha_village).text = "Village: -"
        findViewById<TextView>(R.id.tv_asha_taluka).text = "Taluka: -"
        findViewById<TextView>(R.id.tv_asha_district).text = "District: -"

        val token = TokenManager.getToken()
        if (token.isNotEmpty()) {
            ApiClient.retrofit.getSupervisorProfile("Bearer $token")
                .enqueue(object :
                    retrofit2.Callback<com.sukhayu.patient.data.remote.SupervisorProfile> {
                    override fun onResponse(
                        call: retrofit2.Call<com.sukhayu.patient.data.remote.SupervisorProfile>,
                        response: retrofit2.Response<com.sukhayu.patient.data.remote.SupervisorProfile>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val profile = response.body()!!

                            findViewById<TextView>(R.id.tv_asha_name).text =
                                profile.user_name ?: ashaName
                            findViewById<TextView>(R.id.tvAshaId).text =
                                "ID: ${profile.asha_id ?: ashaId}"
                            findViewById<TextView>(R.id.tv_asha_village).text =
                                "Village: ${profile.village ?: "-"}"
                            findViewById<TextView>(R.id.tv_asha_taluka).text =
                                "Taluka: ${profile.taluka ?: "-"}"
                            findViewById<TextView>(R.id.tv_asha_district).text =
                                "District: ${profile.district ?: "-"}"
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.sukhayu.patient.data.remote.SupervisorProfile>,
                        t: Throwable
                    ) {
                        Log.e("AshaDashboard", "Failed to load profile: ${t.message}")
                    }
                })
        }
    }

    private fun logout() {
        getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
        TokenManager.clearToken()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
