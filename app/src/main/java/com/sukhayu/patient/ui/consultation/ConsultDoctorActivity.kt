package com.sukhayu.patient.ui.consultation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.ai_symptom.CheckSymptomsActivity

class ConsultDoctorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consult_doctor)

        // ------------ Start Questionnaire (ANY view you choose) ------------
        val startView = findViewById<View>(R.id.cardStart) // replace with your ID
        startView?.setOnClickListener {
            startActivity(Intent(this, CheckSymptomsActivity::class.java))
        }

        // ------------ Back Button ------------
        val backButton = findViewById<View>(R.id.backButton)
        backButton?.setOnClickListener { finish() }
    }
}
