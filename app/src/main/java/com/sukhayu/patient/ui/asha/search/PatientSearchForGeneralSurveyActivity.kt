package com.sukhayu.patient.ui.asha.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sukhayu.patient.R
import com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.repository.PatientRepository
import com.sukhayu.utils.VoiceInputHelper
import kotlinx.coroutines.launch

/**
 * Patient Search Activity for General Survey
 *
 * This activity allows ASHA workers to search and select a patient
 * before filling out the General Survey form.
 *
 * Flow:
 * 1. User enters patient name/phone
 * 2. Search local database
 * 3. If multiple matches, show chooser dialog
 * 4. Once patient selected, navigate to GeneralSurveyActivity with patient data
 */
class PatientSearchForGeneralSurveyActivity : AppCompatActivity() {

    private lateinit var etPatientSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    private lateinit var repository: PatientRepository
    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_search_general_survey)

        supportActionBar?.apply {
            title = "Search Patient - General Survey"
            setDisplayHomeAsUpEnabled(true)
        }

        initializeRepository()
        initializeViews()
        setupListeners()

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun initializeRepository() {
        val database = AshaLocalDatabase.getInstance(applicationContext)
        val apiService = ApiClient.retrofit
        repository = PatientRepository(database, apiService)
    }

    private fun initializeViews() {
        etPatientSearch = findViewById(R.id.etPatientSearch)
        btnSearch = findViewById(R.id.btnSearch)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE
    }

    private fun setupListeners() {
        btnSearch.setOnClickListener {
            val query = etPatientSearch.text.toString().trim()
            if (query.isEmpty()) {
                tvError.text = "Please enter patient name or phone number"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            searchPatient(query)
        }

        etPatientSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                tvError.visibility = View.GONE
            }
        }
    }

    private fun searchPatient(query: String) {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        btnSearch.isEnabled = false

        lifecycleScope.launch {
            try {
                // Search using repository (offline-first)
                val token = getAuthToken()
                val patients = repository.searchPatients(query, token)

                progressBar.visibility = View.GONE
                btnSearch.isEnabled = true

                when {
                    patients.isEmpty() -> {
                        tvError.text = "No patient found with name or phone: $query"
                        tvError.visibility = View.VISIBLE
                    }
                    patients.size == 1 -> {
                        // Single match - navigate directly
                        navigateToGeneralSurvey(patients[0])
                    }
                    else -> {
                        // Multiple matches - show chooser
                        showPatientChooserDialog(patients)
                    }
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnSearch.isEnabled = true
                tvError.text = "Error searching patients: ${e.message}"
                tvError.visibility = View.VISIBLE
            }
        }
    }

    private fun getAuthToken(): String? {
        return getSharedPreferences("auth", MODE_PRIVATE).getString("auth_token", null)
    }

    private fun showPatientChooserDialog(patients: List<PatientEntity>) {
        val patientNames = patients.map { "${it.name} - ${it.phone ?: "N/A"}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Patient")
            .setItems(patientNames) { dialog, which ->
                navigateToGeneralSurvey(patients[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToGeneralSurvey(patient: PatientEntity) {
        val intent = Intent(this, GeneralSurveyActivity::class.java).apply {
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_ID, patient.id)
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_NAME, patient.name)
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_PHONE, patient.phone ?: "")
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_GENDER, patient.gender ?: "")
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_AGE, "") // Age not available in PatientEntity
        }
        startActivity(intent)
        finish() // Finish this activity so back button from form goes to previous screen
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}

