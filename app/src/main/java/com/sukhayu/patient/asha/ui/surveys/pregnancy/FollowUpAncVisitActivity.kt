package com.sukhayu.patient.asha.ui.surveys.pregnancy

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FollowUpAncVisitActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PATIENT_ID = "patient_id"
        const val EXTRA_PATIENT_NAME = "patient_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // For now, create a simple placeholder layout
        val textView = TextView(this).apply {
            text = "Follow-up ANC Visit Form\n\n(Coming Soon)"
            textSize = 20f
            setPadding(32, 32, 32, 32)
        }
        setContentView(textView)

        supportActionBar?.apply {
            title = "Follow-up ANC Visit"
            setDisplayHomeAsUpEnabled(true)
        }

        val patientId = intent.getStringExtra(EXTRA_PATIENT_ID)
        val patientName = intent.getStringExtra(EXTRA_PATIENT_NAME)

        Toast.makeText(
            this,
            "Follow-up ANC Visit for: $patientName (ID: $patientId)",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

