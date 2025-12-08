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
import com.sukhayu.patient.R
import com.sukhayu.patient.asha.ui.surveys.AshaViewSurveysActivity
import com.sukhayu.patient.asha.ui.surveys.AshaSurveyHomeActivity
import com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyViewModel
import com.sukhayu.patient.asha.ui.surveys.pregnancy.PregnancySyncViewModel
import com.sukhayu.patient.asha.ui.surveys.tb.TbFollowUpViewModel
import com.sukhayu.patient.asha.ui.surveys.tb.TbScreeningViewModel
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.ui.asha.emergency.EmergencyContactsActivity
import com.sukhayu.patient.ui.asha.family.FamilyListActivity
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

        // ═══════════════════════════════════════════════════════════════════════════
        // Setup header with role display (consistent with Patient/Supervisor dashboards)
        // ═══════════════════════════════════════════════════════════════════════════
        HeaderUtils.setupRoleInHeader(this)

        // --- ViewModels for sync jobs ---
        tbScreeningViewModel = ViewModelProvider(this)[TbScreeningViewModel::class.java]
        tbFollowUpViewModel = ViewModelProvider(this)[TbFollowUpViewModel::class.java]
        generalSurveyViewModel = ViewModelProvider(this)[GeneralSurveyViewModel::class.java]
        pregnacySyncViewModel = ViewModelProvider(this)[PregnancySyncViewModel::class.java]

        // Load and display ASHA profile data
        loadProfileData()

        // --- Card clicks ---
        findViewById<CardView>(R.id.cardTotalPatients).setOnClickListener {
            startActivity(Intent(this, SearchPatientActivity::class.java))
        }

        // My Schedule card
        findViewById<CardView>(R.id.cardCompletedToday).setOnClickListener {
            startActivity(Intent(this, AshaScheduleActivity::class.java))
        }

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

        // ═══════════════════════════════════════════════════════════════════════════
        // LOGOUT BUTTON - Uses centralized logout logic matching Patient/Supervisor
        // Clears all auth data and navigates to LoginActivity with proper flags
        // ═══════════════════════════════════════════════════════════════════════════
        findViewById<Button>(R.id.tv_logout).setOnClickListener {
            performLogout()
        }

        // --- Language toggles ---
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

            // 4) Pregnancy / First ANC sync
            pregnacySyncViewModel.syncPendingPregnancies { count ->
                Log.d(TAG, "Pregnancy (ANC first visit) sync finished. Synced count = $count")
            }
        } else {
            Log.d(TAG, "onResume: No internet. Sync skipped.")
        }
    }

    /**
     * Centralized logout logic matching Patient/Supervisor dashboards.
     *
     * This method:
     * 1. Clears all auth/session data (token, userId, role) from SharedPreferences
     * 2. Clears TokenManager's in-memory state
     * 3. Navigates to LoginActivity with proper flags (NEW_TASK + CLEAR_TASK)
     *    preventing back press from returning to logged-in state
     * 4. Logs each step for debugging in Logcat
     */
    private fun performLogout() {
        Log.d(TAG, "performLogout: Logout button clicked by user")

        // Step 1: Clear all auth/session data from SharedPreferences
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        Log.d(TAG, "performLogout: Clearing auth SharedPreferences (token, role, userId, etc.)")
        prefs.edit().clear().apply()

        // Step 2: Clear TokenManager's in-memory state
        Log.d(TAG, "performLogout: Calling TokenManager.clearToken() to clear in-memory state")
        TokenManager.clearToken()

        // Step 3: Navigate to LoginActivity with proper flags to prevent back press
        Log.d(TAG, "performLogout: Creating Intent to LoginActivity with NEW_TASK + CLEAR_TASK flags")
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        // Step 4: Finish this activity
        finish()
        Log.d(TAG, "performLogout: AshaDashboardActivity finished. User should see LoginActivity now")
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

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}

