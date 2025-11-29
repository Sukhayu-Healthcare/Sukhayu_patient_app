package com.sukhayu.patient.asha.ui.surveys.pregnancy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.repository.PatientRepository

class PregnancySurveyActivity : AppCompatActivity() {

    private lateinit var etPatientName: EditText
    private lateinit var btnLoadPatientDetails: Button
    private lateinit var patientDetailsCard: MaterialCardView
    private lateinit var tvPatientName: TextView
    private lateinit var tvPatientPhone: TextView
    private lateinit var tvPatientGender: TextView
    private lateinit var tvPatientWeight: TextView
    private lateinit var spinnerSurveyType: Spinner
    private lateinit var btnContinueSurvey: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    private lateinit var viewModel: PregnancySurveyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregnancy_survey)

        // Set toolbar title
        supportActionBar?.apply {
            title = "Pregnancy / ANC Survey"
            setDisplayHomeAsUpEnabled(true)
        }

        initializeViewModel()
        initializeViews()
        setupSurveyTypeSpinner()
        setupListeners()
        observeViewModel()
    }

    private fun initializeViewModel() {
        val database = AshaLocalDatabase.getInstance(applicationContext)
        val apiService = ApiClient.retrofit
        val repository = PatientRepository(database, apiService)
        val factory = PregnancySurveyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PregnancySurveyViewModel::class.java]

        // TODO: Set to false once backend is stable
        // For now, enable dummy data if no auth token is available
        val token = getAuthToken()
        viewModel.useDummyData = token == null
    }

    private fun initializeViews() {
        etPatientName = findViewById(R.id.etPatientName)
        btnLoadPatientDetails = findViewById(R.id.btnLoadPatientDetails)
        patientDetailsCard = findViewById(R.id.patientDetailsCard)
        tvPatientName = findViewById(R.id.tvPatientName)
        tvPatientPhone = findViewById(R.id.tvPatientPhone)
        tvPatientGender = findViewById(R.id.tvPatientGender)
        tvPatientWeight = findViewById(R.id.tvPatientWeight)
        spinnerSurveyType = findViewById(R.id.spinnerSurveyType)
        btnContinueSurvey = findViewById(R.id.btnContinueSurvey)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        // Initially hide patient details card, progress bar, and error
        patientDetailsCard.visibility = View.GONE
        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE
    }

    private fun setupSurveyTypeSpinner() {
        val surveyTypes = arrayOf("First ANC Visit", "Follow-up ANC Visit")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, surveyTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSurveyType.adapter = adapter

        spinnerSurveyType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = SurveyType.fromDisplayName(surveyTypes[position])
                selectedType?.let { viewModel.onSurveyTypeSelected(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupListeners() {
        btnLoadPatientDetails.setOnClickListener {
            val query = etPatientName.text.toString().trim()
            val token = getAuthToken()
            viewModel.onLoadPatientClicked(query, token)
        }

        btnContinueSurvey.setOnClickListener {
            viewModel.onContinueClicked()
        }

        // Clear error when user starts typing
        etPatientName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.clearError()
            }
        }
    }

    private fun observeViewModel() {
        // Observe UI state
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Idle -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE
                    btnLoadPatientDetails.isEnabled = true
                    btnContinueSurvey.isEnabled = true
                }
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                    btnLoadPatientDetails.isEnabled = false
                    btnContinueSurvey.isEnabled = false
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                    btnLoadPatientDetails.isEnabled = true
                    btnContinueSurvey.isEnabled = true
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE
                    btnLoadPatientDetails.isEnabled = true
                    btnContinueSurvey.isEnabled = true
                }
            }
        }

        // Observe patient details
        viewModel.patientDetails.observe(this) { patient ->
            if (patient != null) {
                tvPatientName.text = patient.name
                tvPatientPhone.text = patient.phone
                tvPatientGender.text = patient.gender
                tvPatientWeight.text = patient.weight
                patientDetailsCard.visibility = View.VISIBLE
            } else {
                patientDetailsCard.visibility = View.GONE
            }
        }

        // Observe patient chooser dialog
        viewModel.showPatientChooser.observe(this) { event ->
            event.getContentIfNotHandled()?.let { patients ->
                showPatientChooserDialog(patients)
            }
        }

        // Observe navigation events
        viewModel.navigationEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { navEvent ->
                when (navEvent) {
                    is NavigationEvent.NavigateToFirstAncVisit -> {
                        navigateToFirstAncVisit(navEvent.patientId, navEvent.patientName)
                    }
                    is NavigationEvent.NavigateToFollowUpAncVisit -> {
                        navigateToFollowUpAncVisit(navEvent.patientId, navEvent.patientName)
                    }
                }
            }
        }
    }

    private fun showPatientChooserDialog(patients: List<PatientEntity>) {
        val items = patients.map { "${it.name} - ${it.phone ?: "No phone"}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Multiple Patients Found")
            .setItems(items) { dialog, which ->
                viewModel.selectPatient(patients[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToFirstAncVisit(patientId: String, patientName: String) {
        val intent = Intent(this, FirstAncVisitActivity::class.java).apply {
            putExtra(FirstAncVisitActivity.EXTRA_PATIENT_ID, patientId)
            putExtra(FirstAncVisitActivity.EXTRA_PATIENT_NAME, patientName)
        }
        startActivity(intent)
    }

    private fun navigateToFollowUpAncVisit(patientId: String, patientName: String) {
        val intent = Intent(this, FollowUpAncVisitActivity::class.java).apply {
            putExtra(FollowUpAncVisitActivity.EXTRA_PATIENT_ID, patientId)
            putExtra(FollowUpAncVisitActivity.EXTRA_PATIENT_NAME, patientName)
        }
        startActivity(intent)
    }

    private fun getAuthToken(): String? {
        // TODO: Retrieve token from SharedPreferences or secure storage
        // For now, return null - the repository will handle offline-first search
        val sharedPrefs = getSharedPreferences("sukhayu_prefs", MODE_PRIVATE)
        return sharedPrefs.getString("auth_token", null)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

