package com.sukhayu.patient

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val cardProfile = findViewById<CardView>(R.id.cardProfile)
        val cardCheckSymptoms = findViewById<CardView>(R.id.cardCheckSymptoms)
        val cardConsultDoctor = findViewById<CardView>(R.id.cardConsultDoctor)
        val cardMedicines = findViewById<CardView>(R.id.cardMedicines)
        val cardPastConsultations = findViewById<CardView>(R.id.cardPastConsultations)

        // Profile page
        cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        cardCheckSymptoms.setOnClickListener {
            startActivity(Intent(this, CheckSymptomsActivity::class.java))
        }

        cardConsultDoctor.setOnClickListener {
            startActivity(Intent(this, ConsultDoctorActivity::class.java))
        }

        cardMedicines.setOnClickListener {
            startActivity(Intent(this, MedicinesActivity::class.java))
        }

        cardPastConsultations.setOnClickListener {
            startActivity(Intent(this, PastConsultationsActivity::class.java))
        }
    }
}
