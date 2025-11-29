package com.sukhayu.patient.ui.supervisor.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.SupervisorProfile
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.ui.supervisor.registration.RegisterAshaActivity
import com.sukhayu.patient.ui.supervisor.profile.AshaProfileActivity
import com.sukhayu.patient.ui.supervisor.ViewAshaDataActivity
import com.sukhayu.patient.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private lateinit var cardProfile: MaterialCardView

    private var supervisorProfile: SupervisorProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervisor_home)

        // Initialize TokenManager
        TokenManager.init(this)

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
        cardProfile = findViewById(R.id.cardProfile)
    }

    private fun loadProfile() {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        val userName = sharedPref.getString("user_name", "Supervisor")
        val userId = sharedPref.getString("user_id", "N/A")

        if (token != null) {
            ApiClient.retrofit.getSupervisorProfile("Bearer $token")
                .enqueue(object : Callback<SupervisorProfile> {
                    override fun onResponse(
                        call: Call<SupervisorProfile>,
                        response: Response<SupervisorProfile>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            supervisorProfile = response.body()!!
                            val profile = supervisorProfile!!
                            tvPatientName.text = profile.user_name
                            tvPatientId.text = "Supervisor ID: ${profile.asha_id}"
                            if (profile.profile_pic.isNullOrEmpty()) {
                                imgProfile.setImageResource(R.drawable.sample_patient)
                            }
                        } else {
                            setDefaultProfile(userName, userId)
                        }
                    }

                    override fun onFailure(call: Call<SupervisorProfile>, t: Throwable) {
                        Toast.makeText(
                            this@SupervisorHomeActivity,
                            "Failed to load profile",
                            Toast.LENGTH_SHORT
                        ).show()
                        setDefaultProfile(userName, userId)
                    }
                })
        } else {
            setDefaultProfile(userName, userId)
        }
    }

    private fun setDefaultProfile(name: String?, id: String?) {
        tvPatientName.text = name ?: "Supervisor"
        tvPatientId.text = "Supervisor ID: ${id ?: "N/A"}"
        imgProfile.setImageResource(R.drawable.sample_patient)
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            logout()
        }

        cardProfile.setOnClickListener {
            navigateToProfile()
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
        // Clear TokenManager
        TokenManager.clearToken()
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun navigateToProfile() {
        val intent = Intent(this, AshaProfileActivity::class.java)
        if (supervisorProfile != null) {
            intent.putExtra("ashaId", supervisorProfile!!.asha_id)
            intent.putExtra("ashaName", supervisorProfile!!.user_name)
            intent.putExtra("userId", supervisorProfile!!.user_id)
            intent.putExtra("phone", supervisorProfile!!.phone)
            intent.putExtra("village", supervisorProfile!!.village)
            intent.putExtra("district", supervisorProfile!!.district)
            intent.putExtra("taluka", supervisorProfile!!.taluka)
            intent.putExtra("role", "supervisor")
        }
        startActivity(intent)
    }

    private fun navigateToRegisterAsha() {
        startActivity(Intent(this, RegisterAshaActivity::class.java))
    }

    private fun navigateToViewAshaData() {
        val intent = Intent(this, ViewAshaDataActivity::class.java)
        if (supervisorProfile != null) {
            intent.putExtra("supervisor_id", supervisorProfile!!.asha_id)
        }
        startActivity(intent)
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
