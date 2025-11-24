package com.sukhayu.patient.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.ai_symptom.SymptomChatActivity
import com.sukhayu.patient.ui.login.LoginActivity
import android.widget.ImageView
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

        val ivLogout = findViewById<ImageView>(R.id.btnLogout)
        ivLogout.setOnClickListener {
            // Clear user session or preferences if needed

            // Redirect to login activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
}
