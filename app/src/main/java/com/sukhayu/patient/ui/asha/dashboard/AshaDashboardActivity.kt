package com.sukhayu.patient.ui.asha.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.asha.registration.RegisterPatientActivity
import com.sukhayu.patient.ui.asha.nhp.NationalHealthProgramsActivity

class AshaDashboardActivity : AppCompatActivity() {

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
            // Open National Health Programs (or change to proper surveys screen if available)
            startActivity(Intent(this, NationalHealthProgramsActivity::class.java))
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
    }
}
