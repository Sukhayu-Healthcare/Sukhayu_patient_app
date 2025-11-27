package com.sukhayu.patient.ui.supervisor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.login.LoginActivity

class SupervisorHomeActivity : AppCompatActivity() {

    private lateinit var tvPatientName: TextView
    private lateinit var tvPatientId: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnLogout: Button

    // Dashboard cards
    private lateinit var cardRegisterAsha: MaterialCardView
    private lateinit var cardViewAsha: MaterialCardView
    private lateinit var cardCreateSurvey: MaterialCardView
    private lateinit var cardCreateDrive: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervisor_home)

        initViews()
        loadProfile()
        setupListeners()
    }

    private fun initViews() {
        tvPatientName = findViewById(R.id.tvPatientName)
        tvPatientId = findViewById(R.id.tvPatientId)
        imgProfile = findViewById(R.id.image_profile_supervisor)
        btnLogout = findViewById(R.id.btnLogout)

        cardRegisterAsha = findViewById(R.id.cardRegisterAsha)
        cardViewAsha = findViewById(R.id.cardViewAsha)
        cardCreateSurvey = findViewById(R.id.cardCreateSurvey)
        cardCreateDrive = findViewById(R.id.cardCreateDrive)
    }

    private fun loadProfile() {
        val name = "Dummy Supervisor"
        val id = "SUP001"

        tvPatientName.text = name
        tvPatientId.text = "Supervisor ID: $id"
        imgProfile.setImageResource(R.drawable.sample_patient)
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            logout()
        }

        cardRegisterAsha.setOnClickListener {
            navigateToRegisterAsha()
        }

        cardViewAsha.setOnClickListener {
            navigateToViewAshaData()
        }

        cardCreateSurvey.setOnClickListener {
            navigateToCreateSurvey()
        }

        cardCreateDrive.setOnClickListener {
            navigateToCreateDrive()
        }
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun navigateToRegisterAsha() {
        // TODO: Replace with actual RegisterAshaActivity
        // startActivity(Intent(this, RegisterAshaActivity::class.java))
    }

    private fun navigateToViewAshaData() {
        // TODO: Replace with actual ViewAshaDataActivity
        // startActivity(Intent(this, ViewAshaDataActivity::class.java))
    }

    private fun navigateToCreateSurvey() {
        // TODO: Replace with actual CreateSurveyActivity
        // startActivity(Intent(this, CreateSurveyActivity::class.java))
    }

    private fun navigateToCreateDrive() {
        // TODO: Replace with actual CreateDriveActivity
        // startActivity(Intent(this, CreateDriveActivity::class.java))
    }
}
