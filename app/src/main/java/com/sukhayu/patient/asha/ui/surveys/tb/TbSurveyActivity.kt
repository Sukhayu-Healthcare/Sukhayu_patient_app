package com.sukhayu.patient.asha.ui.surveys.tb

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
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.repository.PatientRepository
import com.sukhayu.utils.VoiceInputHelper

/**
 * TbSurveyActivity - Main entry point for TB workflows
 *
 * ASHA flow:
 * 1. Search and select a patient (using shared offline-first PatientRepository)
 * 2. View patient details
 * 3. Choose TB form type:
 *    - TB Screening / Suspect Form
 *    - TB Treatment Follow-up (DOTS)
 *
 * IMPORTANT: This activity was fixed to restore button click functionality after a merge conflict.
 * The following were restored:
 * - ViewModel initialization (TbSurveyViewModel + TbSurveyViewModelFactory)
 * - View binding via findViewById for all buttons and UI elements
 * - Button click listeners in setupListeners()
 * - LiveData observers in observeViewModel() for UI state, patient details, and navigation events
 */
class TbSurveyActivity : AppCompatActivity() {

    private lateinit var etPatientName: EditText
    private lateinit var btnLoadPatientDetails: Button
    private lateinit var patientDetailsCard: MaterialCardView
    private lateinit var tvPatientName: TextView
    private lateinit var tvPatientPhone: TextView
    private lateinit var tvPatientGender: TextView
    private lateinit var tvPatientWeight: TextView
    private lateinit var btnTbScreening: Button
    private lateinit var btnTbFollowUp: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    private lateinit var viewModel: TbSurveyViewModel
    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tb_survey)

        supportActionBar?.apply {
            title = "TB Survey"
            setDisplayHomeAsUpEnabled(true)
        }

        initializeViewModel()
        initializeViews()
        setupListeners()
        observeViewModel()

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun initializeViewModel() {
        val database = AshaLocalDatabase.getInstance(applicationContext)
        val apiService = ApiClient.retrofit
        val repository = PatientRepository(database, apiService)
        val factory = TbSurveyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TbSurveyViewModel::class.java]
    }

    private fun initializeViews() {
        etPatientName = findViewById(R.id.etPatientName)
        btnLoadPatientDetails = findViewById(R.id.btnLoadPatientDetails)
        patientDetailsCard = findViewById(R.id.patientDetailsCard)
        tvPatientName = findViewById(R.id.tvPatientName)
        tvPatientPhone = findViewById(R.id.tvPatientPhone)
        tvPatientGender = findViewById(R.id.tvPatientGender)
        tvPatientWeight = findViewById(R.id.tvPatientWeight)
        btnTbScreening = findViewById(R.id.btnTbScreening)
        btnTbFollowUp = findViewById(R.id.btnTbFollowUp)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        // Initially hide patient details card, progress bar, and error
        patientDetailsCard.visibility = View.GONE
        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE
    }

    private fun setupListeners() {
        btnLoadPatientDetails.setOnClickListener {
            val query = etPatientName.text.toString().trim()
            val token = getAuthToken()
            viewModel.onLoadPatientClicked(query, token)
        }

        btnTbScreening.setOnClickListener {
            viewModel.onTbScreeningClicked()
        }

        btnTbFollowUp.setOnClickListener {
            viewModel.onTbFollowUpClicked()
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
                    btnTbScreening.isEnabled = viewModel.isPatientLoaded
                    btnTbFollowUp.isEnabled = viewModel.isPatientLoaded
                }
                is UiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                    btnLoadPatientDetails.isEnabled = false
                    btnTbScreening.isEnabled = false
                    btnTbFollowUp.isEnabled = false
                }
                is UiState.Error -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                    btnLoadPatientDetails.isEnabled = true
                    btnTbScreening.isEnabled = viewModel.isPatientLoaded
                    btnTbFollowUp.isEnabled = viewModel.isPatientLoaded
                }
                is UiState.Success -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE
                    btnLoadPatientDetails.isEnabled = true
                    btnTbScreening.isEnabled = viewModel.isPatientLoaded
                    btnTbFollowUp.isEnabled = viewModel.isPatientLoaded
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
                    is NavigationEvent.NavigateToTbScreening -> {
                        navigateToTbScreening(navEvent)
                    }
                    is NavigationEvent.NavigateToTbFollowUp -> {
                        navigateToTbFollowUp(navEvent)
                    }
                }
            }
        }
    }

    private fun showPatientChooserDialog(patients: List<PatientEntity>) {
        val patientNames = patients.map { "${it.name} - ${it.phone ?: "N/A"}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Multiple Patients Found")
            .setItems(patientNames) { _, which ->
                viewModel.selectPatient(patients[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToTbScreening(event: NavigationEvent.NavigateToTbScreening) {
        val intent = Intent(this, TbScreeningActivity::class.java).apply {
            putExtra("patient_id", event.patientId)
            putExtra("patient_name", event.patientName)
            putExtra("patient_phone", event.patientPhone)
            putExtra("patient_gender", event.patientGender)
            putExtra("patient_weight", event.patientWeight)
            putExtra("template_id", event.templateId)
        }
        startActivity(intent)
    }

    private fun navigateToTbFollowUp(event: NavigationEvent.NavigateToTbFollowUp) {
        val intent = Intent(this, TbFollowUpActivity::class.java).apply {
            putExtra("patient_id", event.patientId)
            putExtra("patient_name", event.patientName)
            putExtra("patient_phone", event.patientPhone)
            putExtra("patient_gender", event.patientGender)
            putExtra("patient_weight", event.patientWeight)
            putExtra("template_id", event.templateId)
        }
        startActivity(intent)
    }

    private fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("sukhayu_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
