package com.sukhayu.patient.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.ai_symptom.SymptomChatActivity
import com.google.android.material.card.MaterialCardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val cardCheckSymptoms = findViewById<MaterialCardView>(R.id.cardCheckSymptoms)

        cardCheckSymptoms.setOnClickListener {
            val intent = Intent(this, SymptomChatActivity::class.java)
            startActivity(intent)
        }
    }
}
