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
import android.widget.Toast
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
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
import com.sukhayu.patient.ui.asha.emergency.EmergencyContactsActivity
import com.sukhayu.patient.ui.asha.nhp.NationalHealthProgramsActivity
import com.sukhayu.patient.ui.asha.registration.RegisterPatientActivity
import com.sukhayu.patient.ui.asha.schedule.AshaScheduleActivity
import com.sukhayu.patient.ui.asha.search.SearchPatientActivity
import com.sukhayu.patient.ui.login.LoginActivity
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
        val lang = prefs.getString("app_lang", "mr") ?: "mr"
        val wrapped = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(wrapped)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_dashboard)

        // ---- Toolbar + menu clicks (Profile + Language + Logout) ----
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_profile -> {
                    startActivity(Intent(this, AshaProfileActivity::class.java))
                    true
                }
                R.id.menu_language -> {
                    showLanguageDialog()
                    true
                }
                R.id.menu_logout -> {
                    performLogout()
                    true
                }
                else -> false
            }
        }

        // ---- ViewModels for sync ----
        tbScreeningViewModel = ViewModelProvider(this)[TbScreeningViewModel::class.java]
        tbFollowUpViewModel = ViewModelProvider(this)[TbFollowUpViewModel::class.java]
        generalSurveyViewModel = ViewModelProvider(this)[GeneralSurveyViewModel::class.java]
        pregnacySyncViewModel = ViewModelProvider(this)[PregnancySyncViewModel::class.java]

        // ---- Card clicks ----
        findViewById<CardView>(R.id.cardTotalPatients).setOnClickListener {
            startActivity(Intent(this, SearchPatientActivity::class.java))
        }

        findViewById<CardView>(R.id.cardCompletedToday).setOnClickListener {
            startActivity(Intent(this, AshaScheduleActivity::class.java))
        }

        findViewById<CardView>(R.id.cardEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyContactsActivity::class.java))
        }

        // ---- Buttons ----
        findViewById<Button>(R.id.btn_view_surveys).setOnClickListener {
            startActivity(Intent(this, AshaViewSurveysActivity::class.java))
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

        // ---- Voice input ----
        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    override fun onResume() {
        super.onResume()

        if (isNetworkAvailable()) {
            Log.d(TAG, "onResume: Network available, starting sync jobs")

            tbScreeningViewModel.syncPendingTbScreenings { count ->
                Log.d(TAG, "TB screening sync finished. Synced count = $count")
            }

            tbFollowUpViewModel.syncPendingTbFollowUps { count ->
                Log.d(TAG, "TB follow-up sync finished. Synced count = $count")
            }

            pregnacySyncViewModel.syncPendingPregnancies { count ->
                Log.d(TAG, "Pregnancy (ANC first visit) sync finished. Synced count = $count")
            }
        } else {
            Log.d(TAG, "onResume: No internet. Sync skipped.")
        }
    }

    // ---- Logout: clear auth + go to LoginActivity ----
    private fun performLogout() {
        Log.d(TAG, "performLogout: Logout from menu")

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().clear().apply()

        TokenManager.clearToken()

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
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

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Dialog to pick language and save `app_lang` in SharedPreferences.
     */
    private fun showLanguageDialog() {
        val languages = arrayOf("मराठी", "English", "हिन्दी", "ગુજરાતી")
        val codes = arrayOf("mr", "en", "hi", "gu")

        AlertDialog.Builder(this)
            .setTitle("भाषा निवडा / Choose Language")
            .setItems(languages) { _, which ->
                val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                prefs.edit().putString("app_lang", codes[which]).apply()
                recreate()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
