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
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.sukhayu.patient.R
import com.sukhayu.patient.asha.ui.surveys.AshaSurveyHomeActivity
import com.sukhayu.patient.asha.ui.surveys.AshaViewSurveysActivity
import com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyViewModel
import com.sukhayu.patient.asha.ui.surveys.pregnancy.PregnancySyncViewModel
import com.sukhayu.patient.asha.ui.surveys.tb.TbFollowUpViewModel
import com.sukhayu.patient.asha.ui.surveys.tb.TbScreeningViewModel
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.ui.asha.emergency.EmergencyContactsActivity
import com.sukhayu.patient.ui.asha.nhp.NationalHealthProgramsActivity
import com.sukhayu.patient.ui.asha.registration.RegisterPatientActivity
import com.sukhayu.patient.ui.asha.schedule.AshaScheduleActivity
import com.sukhayu.patient.ui.asha.search.SearchPatientActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.utils.HeaderUtils
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.utils.LocaleHelper
import com.sukhayu.utils.VoiceInputHelper

class AshaDashboardActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper

    // Sync ViewModels
    private lateinit var tbScreeningViewModel: TbScreeningViewModel
    private lateinit var tbFollowUpViewModel: TbFollowUpViewModel
    private lateinit var generalSurveyViewModel: GeneralSurveyViewModel
    private lateinit var pregnacySyncViewModel: PregnancySyncViewModel

    companion object {
        private const val TAG = "AshaDashboard"
    }

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

        // Setup common header role text (if header include is present)
        HeaderUtils.setupRoleInHeader(this)

        // Setup toolbar with menu (Profile, Daily Tasks, Settings, Logout)
        // Setup toolbar with menu (Profile, Daily Tasks, Settings, Logout)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {

                R.id.menu_profile -> {
                    Toast.makeText(
                        this,
                        "Profile details are shown here.",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }

                R.id.menu_daily_tasks -> {
                    Toast.makeText(
                        this,
                        "Daily Tasks will be added soon.",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }

                R.id.menu_settings -> {
                    Toast.makeText(
                        this,
                        "Settings will be added soon.",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }

                R.id.menu_logout -> {
                    performLogout()
                    true
                }

                else -> false
            }
        }


        // --- ViewModels for sync jobs ---
        tbScreeningViewModel = ViewModelProvider(this)[TbScreeningViewModel::class.java]
        tbFollowUpViewModel = ViewModelProvider(this)[TbFollowUpViewModel::class.java]
        generalSurveyViewModel = ViewModelProvider(this)[GeneralSurveyViewModel::class.java]
        pregnacySyncViewModel = ViewModelProvider(this)[PregnancySyncViewModel::class.java]

        // Load and display ASHA profile data
        loadProfileData()

        // --- Card clicks ---

        // Total patients / search patient
        findViewById<CardView>(R.id.cardTotalPatients).setOnClickListener {
            startActivity(Intent(this, SearchPatientActivity::class.java))
        }

        // My Schedule card
        findViewById<CardView>(R.id.cardCompletedToday).setOnClickListener {
            startActivity(Intent(this, AshaScheduleActivity::class.java))
        }

        // Emergency contacts
        findViewById<CardView>(R.id.cardEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyContactsActivity::class.java))
        }

        // --- Buttons ---

        // View Surveys screen
        findViewById<Button>(R.id.btn_view_surveys).setOnClickListener {
            startActivity(Intent(this, AshaViewSurveysActivity::class.java))
        }

        // Conduct Survey → AshaSurveyHomeActivity
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

        // Health drives (NHP)
        findViewById<Button>(R.id.btn_health_drives).setOnClickListener {
            startActivity(Intent(this, NationalHealthProgramsActivity::class.java))
        }

        // Register patient
        findViewById<Button>(R.id.btn_register_patient).setOnClickListener {
            startActivity(Intent(this, RegisterPatientActivity::class.java))
        }

        // Setup language toggle in header
        setupLanguageToggle()

        // Voice input
        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    override fun onResume() {
        super.onResume()

        if (isNetworkAvailable()) {
            Log.d(TAG, "onResume: Network available, starting sync jobs")

            // 1) TB Screening sync
            tbScreeningViewModel.syncPendingTbScreenings { count ->
                Log.d(TAG, "TB screening sync finished. Synced count = $count")
            }

            // 2) TB Follow-up sync
            tbFollowUpViewModel.syncPendingTbFollowUps { count ->
                Log.d(TAG, "TB follow-up sync finished. Synced count = $count")
            }

            // 3) Pregnancy / First ANC sync
            pregnacySyncViewModel.syncPendingPregnancies { count ->
                Log.d(TAG, "Pregnancy (ANC first visit) sync finished. Synced count = $count")
            }
        } else {
            Log.d(TAG, "onResume: No internet. Sync skipped.")
        }
    }

    /**
     * Centralized logout logic.
     */
    private fun performLogout() {
        Log.d(TAG, "performLogout: Logout selected from menu")

        // Step 1: Clear all auth/session data from SharedPreferences
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Step 2: Clear TokenManager's in-memory state
        TokenManager.clearToken()

        // Step 3: Navigate to LoginActivity with proper flags to prevent back press
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        // Step 4: Finish this activity
        finish()
    }

    private fun requestAudioPermission() {
        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
        }
    }

    /**
     * Loads ASHA profile data and uses string resources so labels
     * auto-translate between English and Marathi.
     */
    private fun loadProfileData() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)

        val defaultAshaName = getString(R.string.asha_default_name)
        val ashaName = prefs.getString("user_name", defaultAshaName) ?: defaultAshaName
        val ashaId = prefs.getString("user_id", "N/A") ?: "N/A"

        val tvName = findViewById<TextView>(R.id.tv_asha_name)
        val tvId = findViewById<TextView>(R.id.tvAshaId)
        val tvVillage = findViewById<TextView>(R.id.tv_asha_village)
        val tvTaluka = findViewById<TextView>(R.id.tv_asha_taluka)
        val tvDistrict = findViewById<TextView>(R.id.tv_asha_district)

        // Default values
        tvName.text = ashaName
        tvId.text = getString(R.string.asha_id_format, ashaId)
        tvVillage.text = getString(R.string.village_format, "-")
        tvTaluka.text = getString(R.string.taluka_format, "-")
        tvDistrict.text = getString(R.string.district_format, "-")

        // Fetch complete profile from API
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

                            tvName.text = profile.user_name ?: ashaName

                            val finalAshaId = profile.asha_id ?: ashaId
                            tvId.text = getString(R.string.asha_id_format, finalAshaId)

                            tvVillage.text = getString(
                                R.string.village_format,
                                profile.village ?: "-"
                            )
                            tvTaluka.text = getString(
                                R.string.taluka_format,
                                profile.taluka ?: "-"
                            )
                            tvDistrict.text = getString(
                                R.string.district_format,
                                profile.district ?: "-"
                            )
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.sukhayu.patient.data.remote.SupervisorProfile>,
                        t: Throwable
                    ) {
                        Log.e(TAG, "Failed to load profile: ${t.message}")
                        // Keep default values
                    }
                })
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Sets up the language toggle buttons in the header.
     * Uses "settings" → "app_lang" just like patient dashboard.
     */
    private fun setupLanguageToggle() {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
