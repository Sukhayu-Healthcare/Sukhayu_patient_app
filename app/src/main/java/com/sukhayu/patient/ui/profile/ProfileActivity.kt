package com.sukhayu.patient.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.R

class ProfileActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PATIENT_ID = "patientId"
        const val EXTRA_PATIENT_NAME = "patientName"
    }

    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etPhone: EditText
    private lateinit var etPatientId: EditText
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // find views
        etName = findViewById(R.id.et_name)
        etAge = findViewById(R.id.et_age)
        spinnerGender = findViewById(R.id.spinner_gender)
        etPhone = findViewById(R.id.et_phone)
        etPatientId = findViewById(R.id.et_patient_id)
        btnEdit = findViewById(R.id.btn_edit)
        btnLogout = findViewById(R.id.btn_logout)

        // read extras with safe fallbacks
        val patientId = intent.getStringExtra(EXTRA_PATIENT_ID) ?: "P001"
        val patientName = intent.getStringExtra(EXTRA_PATIENT_NAME) ?: "Dummy Patient"

        // populate UI
        etPatientId.setText(patientId)
        etName.setText(patientName)
        etAge.setText("45") // placeholder, replace with real data load later
        etPhone.setText("+91-9876543210")

        // Edit toggle behaviour
        btnEdit.setOnClickListener {
            val nowEnabled = !etName.isEnabled
            etName.isEnabled = nowEnabled
            etAge.isEnabled = nowEnabled
            spinnerGender.isEnabled = nowEnabled
            etPhone.isEnabled = nowEnabled
            btnEdit.text = if (nowEnabled) "Save" else "Edit"

            if (!nowEnabled) {
                Toast.makeText(this, "Profile saved (placeholder)", Toast.LENGTH_SHORT).show()
                // TODO: save to DB or API using patientId
            }
        }

        // Logout -> LoginActivity
        btnLogout.setOnClickListener {
            getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
