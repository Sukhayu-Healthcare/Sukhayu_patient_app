package com.sukhayu.patient.ui.asha.dashboard


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R

/**
 * Compile-safe AshaDashboardActivity:
 * - Uses string-based Intents (setClassName) to avoid unresolved imports at compile time.
 * - Replace package names in the strings below with exact fully-qualified class names
 *   if you want to use direct class references later.
 */

class AshaDashboardActivity : AppCompatActivity() {

    // helper to open activity by its full name; safe at compile-time
    private fun openByClassName(targetClass: String) {
        try {
            val intent = Intent()
            // package name (first arg) should be your app's applicationId/package
            intent.setClassName(this.packageName, targetClass)
            startActivity(intent)
        } catch (e: Exception) {
            // fails quietly for now; you can log or show a toast
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_dashboard)

        // header ASHA name
        val tvAshaName = findViewById<TextView>(R.id.tv_asha_name)
        tvAshaName.text = getString(R.string.sample_asha_name) // resource-based

        // static metrics (replace with real data later)
        findViewById<TextView>(R.id.tv_total_patients).text = "12"
        findViewById<TextView>(R.id.tv_active_cases).text = "7"
        findViewById<TextView>(R.id.tv_completed_today).text = "0"
        findViewById<TextView>(R.id.tv_emergency_cases).text = "2"

        // buttons
        findViewById<Button>(R.id.btn_view_surveys).setOnClickListener {
            // open ChildSurveyActivity using class name string
            openByClassName("com.sukhayu.patient.asha.surveys.ChildSurveyActivity")
        }

        findViewById<Button>(R.id.btn_health_drives).setOnClickListener {
            openByClassName("com.sukhayu.patient.asha.nhp.NationalHealthProgramsActivity")
        }

        findViewById<Button>(R.id.btn_register_patient).setOnClickListener {
            openByClassName("com.sukhayu.patient.asha.registration.RegisterPatientActivity")
        }

        findViewById<Button>(R.id.btn_help_login).setOnClickListener {
            // TODO: open help screen when ready
        }

        // optional bottom quick survey buttons
        findViewById<Button>(R.id.btn_open_maternal).setOnClickListener {
            openByClassName("com.sukhayu.patient.asha.surveys.MaternalSurveyActivity")
        }
        findViewById<Button>(R.id.btn_open_vaccination).setOnClickListener {
            openByClassName("com.sukhayu.patient.asha.surveys.VaccinationSurveyActivity")
        }
    }
}
