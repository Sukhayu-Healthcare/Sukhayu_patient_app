package com.sukhayu.patient.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.google.android.material.card.MaterialCardView
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.ai_symptom.SymptomChatActivity
import com.sukhayu.patient.DoctorDetailActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.ConsultDoctorActivity
import com.sukhayu.patient.PastConsultationsActivity
import com.sukhayu.patient.MedicinesActivity
import com.sukhayu.patient.ProfileActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // ---------------------------
        //  PROFILE CARD
        // ---------------------------
        val cardProfile = findViewById<MaterialCardView>(R.id.cardProfile)
        cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // ---------------------------
        //  AI SYMPTOM CHECKER
        // ---------------------------
        val cardCheckSymptoms = findViewById<MaterialCardView>(R.id.cardCheckSymptoms)
        cardCheckSymptoms.setOnClickListener {
            startActivity(Intent(this, SymptomChatActivity::class.java))
        }

        // ---------------------------
        //  CONSULT DOCTOR
        // ---------------------------
        val cardConsultDoctor = findViewById<MaterialCardView>(R.id.cardConsultDoctor)
        cardConsultDoctor.setOnClickListener {
            startActivity(Intent(this, ConsultDoctorActivity::class.java))
        }

        // ---------------------------
        //  PAST CONSULTATIONS
        // ---------------------------
        val cardPast = findViewById<MaterialCardView>(R.id.cardPastConsultations)
        cardPast.setOnClickListener {
            startActivity(Intent(this, PastConsultationsActivity::class.java))
        }

        // ---------------------------
        //  MEDICINES
        // ---------------------------
        val cardMedicines = findViewById<MaterialCardView>(R.id.cardMedicines)
        cardMedicines.setOnClickListener {
            startActivity(Intent(this, MedicinesActivity::class.java))
        }

        // ---------------------------
        //  EMERGENCY BUTTON
        // ---------------------------
        val btnEmergency = findViewById<Button>(R.id.btnEmergency)
        btnEmergency.setOnClickListener {
            // You can show a dialog or open emergency activity
            // For now launching PastConsultationsActivity just as placeholder
            startActivity(Intent(this, PastConsultationsActivity::class.java))
        }

        // ---------------------------
        //  LOGOUT BUTTON
        // ---------------------------
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            getSharedPreferences("auth", MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
