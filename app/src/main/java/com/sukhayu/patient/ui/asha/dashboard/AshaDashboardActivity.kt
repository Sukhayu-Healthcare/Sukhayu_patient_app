package com.sukhayu.patient.ui.asha.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
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
import com.sukhayu.patient.ui.asha.nhp.NationalHealthProgramsActivity
import com.sukhayu.patient.ui.asha.registration.RegisterPatientActivity
import com.sukhayu.patient.asha.ui.surveys.AshaSurveyHomeActivity
import com.sukhayu.patient.ui.asha.family.FamilyListActivity
import com.sukhayu.patient.ui.asha.emergency.EmergencyContactsActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.utils.VoiceInputHelper
import com.sukhayu.utils.LocaleHelper
import com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyViewModel

class AshaDashboardActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper

    // ViewModel for syncing General Surveys
    private lateinit var generalSurveyViewModel: GeneralSurveyViewModel

    // Network monitoring
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

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

        // Init ViewModel for general survey sync
        generalSurveyViewModel =
            ViewModelProvider(this)[GeneralSurveyViewModel::class.java]

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

        // Logout button
        findViewById<Button>(R.id.tv_logout).setOnClickListener {
            logout()
        }

        // Language toggles: save selection and recreate activity to apply new locale
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

        // 🔌 Setup network listener so we sync when internet comes back
        setupNetworkCallback()
    }

    override fun onResume() {
        super.onResume()

        // Backup: also try syncing whenever dashboard comes to foreground
        if (isNetworkAvailable()) {
            Log.d("AshaDashboard", "onResume: Network available, syncing pending general surveys")
            generalSurveyViewModel.syncPendingSurveys { count ->
                Log.d("AshaDashboard", "onResume: Synced $count pending general surveys")
            }
        } else {
            Log.d("AshaDashboard", "onResume: No internet. General survey sync skipped.")
        }
    }

    private fun setupNetworkCallback() {
        connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d("AshaDashboard", "NetworkCallback: Network became AVAILABLE")
                    runOnUiThread {
                        generalSurveyViewModel.syncPendingSurveys { count ->
                            Log.d(
                                "AshaDashboard",
                                "NetworkCallback: Synced $count pending general surveys"
                            )
                            // If you want to SEE something while testing, uncomment:
                            // Toast.makeText(this@AshaDashboardActivity, "Synced $count general surveys", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d("AshaDashboard", "NetworkCallback: Network LOST")
                }
            }

            try {
                connectivityManager?.registerDefaultNetworkCallback(networkCallback!!)
                Log.d("AshaDashboard", "NetworkCallback: Registered default network callback")
            } catch (e: Exception) {
                Log.e("AshaDashboard", "Failed to register network callback: ${e.message}", e)
            }
        } else {
            // For older versions, we only rely on onResume + isNetworkAvailable()
            Log.d(
                "AshaDashboard",
                "NetworkCallback: API < 24, using onResume() based sync only"
            )
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

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val activeNetwork = cm.activeNetworkInfo
            @Suppress("DEPRECATION")
            activeNetwork != null && activeNetwork.isConnected
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                if (networkCallback != null) {
                    connectivityManager?.unregisterNetworkCallback(networkCallback!!)
                    Log.d("AshaDashboard", "NetworkCallback: Unregistered")
                }
            } catch (e: Exception) {
                Log.e("AshaDashboard", "Error unregistering network callback: ${e.message}", e)
            }
        }
    }
}
