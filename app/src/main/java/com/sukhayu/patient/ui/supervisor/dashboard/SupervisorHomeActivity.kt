package com.sukhayu.patient.ui.supervisor.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.SupervisorProfile
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.ui.supervisor.ViewAshaDataActivity
import com.sukhayu.patient.ui.supervisor.ViewSurveysAndDrivesActivity
import com.sukhayu.patient.ui.supervisor.profile.SupervisorAshaProfileActivity
import com.sukhayu.patient.ui.supervisor.registration.RegisterAshaActivity
// import com.sukhayu.patient.ui.supervisor.surveys.CreateSurveyActivity
import com.sukhayu.patient.ui.supervisor.drives.CreateDriveActivity
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.patient.utils.HeaderUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SupervisorHomeActivity : AppCompatActivity() {

    private lateinit var tvPatientName: TextView
    private lateinit var tvPatientId: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnLogout: Button

    private lateinit var cardRegisterAsha: MaterialCardView
    private lateinit var cardViewAsha: MaterialCardView
    // private lateinit var cardCreateSurvey: MaterialCardView
    private lateinit var cardCreateDrive: MaterialCardView
    private lateinit var cardProfile: MaterialCardView
    private lateinit var cardViewSurveysAndDrives: MaterialCardView

    private var supervisorProfile: SupervisorProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervisor_home)
        // In onCreate() after setContentView()
        HeaderUtils.setupRoleInHeader(this)
        TokenManager.init(this)

        initViews()
        loadProfile()
        setupListeners()
        requestAudioPermission()
    }

    private fun initViews() {
        tvPatientName = findViewById(R.id.tvPatientName)
        tvPatientId = findViewById(R.id.tvPatientId)
        imgProfile = findViewById(R.id.image_profile_supervisor)
        btnLogout = findViewById(R.id.btnLogout)

        cardRegisterAsha = findViewById(R.id.cardRegisterAsha)
        cardViewAsha = findViewById(R.id.cardViewAsha)
        // cardCreateSurvey = findViewById(R.id.cardCreateSurvey)
        cardCreateDrive = findViewById(R.id.cardCreateDrive)
        cardProfile = findViewById(R.id.cardProfile)
        cardViewSurveysAndDrives = findViewById(R.id.cardViewSurveysAndDrives)
    }

    private fun loadProfile() {
        val token = TokenManager.getToken()

        if (token.isEmpty()) {
            setDefaultProfile()
            return
        }

        ApiClient.retrofit.getSupervisorProfile("Bearer $token")
            .enqueue(object : Callback<SupervisorProfile> {
                override fun onResponse(
                    call: Call<SupervisorProfile>,
                    response: Response<SupervisorProfile>
                ) {
                    if (response.isSuccessful && response.body() != null) {

                        supervisorProfile = response.body()!!
                        val profile = supervisorProfile!!

                        tvPatientName.text = profile.user_name ?: "Supervisor"
                        tvPatientId.text = "Supervisor ID: ${profile.asha_id ?: "--"}"

                        imgProfile.setImageResource(R.drawable.sample_patient)

                    } else {
                        setDefaultProfile()
                    }
                }

                override fun onFailure(call: Call<SupervisorProfile>, t: Throwable) {
                    Toast.makeText(this@SupervisorHomeActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    setDefaultProfile()
                }
            })
    }

    private fun setDefaultProfile() {
        tvPatientName.text = "Supervisor"
        tvPatientId.text = "Supervisor ID: --"
        imgProfile.setImageResource(R.drawable.sample_patient)
    }

    private fun setupListeners() {

        btnLogout.setOnClickListener { logout() }

        cardProfile.setOnClickListener {
            val intent = Intent(this, SupervisorAshaProfileActivity::class.java)
            startActivity(intent)
        }

        cardRegisterAsha.setOnClickListener {
            startActivity(Intent(this, RegisterAshaActivity::class.java))
        }

        cardViewAsha.setOnClickListener {
            startActivity(Intent(this, ViewAshaDataActivity::class.java))
        }

        // cardCreateSurvey.setOnClickListener {
        //     startActivity(Intent(this, CreateSurveyActivity::class.java))
        // }

        cardCreateDrive.setOnClickListener {
            startActivity(Intent(this, CreateDriveActivity::class.java))
        }

        cardViewSurveysAndDrives.setOnClickListener {
            startActivity(Intent(this, ViewSurveysAndDrivesActivity::class.java))
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    private fun logout() {
        TokenManager.clearToken()

        val i = Intent(this, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
