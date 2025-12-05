package com.sukhayu.patient.asha.ui.surveys.pregnancy

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.toAncFollowUpRequest
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.repository.AncVisitRepository
import com.sukhayu.patient.data.repository.PregnancyRepository
import com.sukhayu.patient.databinding.ActivityFollowUpAncVisitBinding
import com.sukhayu.utils.VoiceInputHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.data.repository.ResultState
import java.text.SimpleDateFormat
import com.sukhayu.patient.utils.HeaderUtils
import java.util.*

class FollowUpAncVisitActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PREGNANCY_ID = "pregnancy_id"
        const val EXTRA_PATIENT_ID = "patient_id"
        const val EXTRA_PATIENT_NAME = "patient_name"
        const val EXTRA_PATIENT_PHONE = "patient_phone"
        const val EXTRA_PATIENT_GENDER = "patient_gender"
        const val EXTRA_PATIENT_WEIGHT = "patient_weight"
    }

    private lateinit var binding: ActivityFollowUpAncVisitBinding
    private lateinit var viewModel: FollowUpAncVisitViewModel

    private var patientId: String? = null
    private var pregnancyId: String? = null
    private var patientName: String? = null
    private var patientPhone: String? = null
    private var patientGender: String? = null
    private var patientWeight: String? = null

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowUpAncVisitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "Follow-up ANC Visit"
            setDisplayHomeAsUpEnabled(true)
        }

        // Initialize ViewModel
        val database = AshaLocalDatabase.getInstance(this)
        val ancVisitDao = database.ancVisitDao()
        val pregnancyDao = database.pregnancyDao()
        val apiService = ApiClient.retrofit

        val ancVisitRepository = AncVisitRepository(ancVisitDao)
        val pregnancyRepository = PregnancyRepository(pregnancyDao, apiService)

        val factory = FollowUpAncVisitViewModelFactory(ancVisitRepository, pregnancyRepository)
        viewModel = ViewModelProvider(this, factory)[FollowUpAncVisitViewModel::class.java]

        // 1) Read Intent extras and fill header
        readIntentExtrasAndFillHeader()

        // 2) Setup date pickers
        setupDatePickers()

        // 3) Setup TT/TD dose dropdown
        setupTtDoseDropdown()

        // 4) Setup conditional fields (BP, Referral)
        setupConditionalFields()

        // 5) Setup symptoms logic ("None of the above")
        setupSymptomsLogic()

        // 6) Observe ViewModel
        observeViewModel()

        // 7) Setup save button with validation
        setupSaveButton()

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
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

    private fun readIntentExtrasAndFillHeader() {
        patientId = intent.getStringExtra(EXTRA_PATIENT_ID)
        pregnancyId = intent.getStringExtra(EXTRA_PREGNANCY_ID)
        patientName = intent.getStringExtra(EXTRA_PATIENT_NAME)
        patientPhone = intent.getStringExtra(EXTRA_PATIENT_PHONE)
        patientGender = intent.getStringExtra(EXTRA_PATIENT_GENDER)
        patientWeight = intent.getStringExtra(EXTRA_PATIENT_WEIGHT)

        // fallback: if pregnancyId is null/blank, use patientId
        if (pregnancyId.isNullOrBlank() && !patientId.isNullOrBlank()) {
            pregnancyId = patientId
        }

        binding.tvPatientNameHeader.text = "Name: " + (patientName ?: "-")
        binding.tvPatientPhoneHeader.text = "Phone: " + (patientPhone ?: "-")
        binding.tvPatientGenderHeader.text = "Gender: " + (patientGender ?: "-")
        binding.tvPatientWeightHeader.text = "Weight: " + (patientWeight ?: "-")
    }

    private fun setupDatePickers() {
        // Visit Date - default to today
        binding.etVisitDate.setOnClickListener {
            showDatePicker { date ->
                binding.etVisitDate.setText(dateFormat.format(date))
            }
        }
        // Set today's date as default
        binding.etVisitDate.setText(dateFormat.format(Date()))

        // Next Visit Date (optional)
        binding.etNextVisitDate.setOnClickListener {
            showDatePicker { date ->
                binding.etNextVisitDate.setText(dateFormat.format(date))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                onDateSelected(calendar.time)
            },
            year,
            month,
            day
        ).show()
    }

    private fun setupTtDoseDropdown() {
        val ttDoseOptions = arrayOf("None", "First", "Second", "Booster")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ttDoseOptions)
        binding.autoTtDose.setAdapter(adapter)
        binding.autoTtDose.setText("None", false)
    }

    private fun setupConditionalFields() {
        // BP Recorded toggle
        binding.switchBpRecorded.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tilBpSystolic.visibility = View.VISIBLE
                binding.tilBpDiastolic.visibility = View.VISIBLE
            } else {
                binding.tilBpSystolic.visibility = View.GONE
                binding.tilBpDiastolic.visibility = View.GONE
                binding.etBpSystolic.text?.clear()
                binding.etBpDiastolic.text?.clear()
            }
        }

        // Referral Made toggle
        binding.switchReferralMade.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tilReferralReason.visibility = View.VISIBLE
            } else {
                binding.tilReferralReason.visibility = View.GONE
                binding.etReferralReason.text?.clear()
            }
        }
    }

    private fun setupSymptomsLogic() {
        // When "None of the above" is checked, uncheck all other symptoms
        binding.cbSymptomNone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.cbSymptomBleeding.isChecked = false
                binding.cbSymptomHeadacheBlurredVision.isChecked = false
                binding.cbSymptomSwelling.isChecked = false
                binding.cbSymptomFeverChills.isChecked = false
                binding.cbSymptomReducedMovements.isChecked = false
                binding.cbSymptomSevereAbdominalPain.isChecked = false
            }
        }

        // When any other symptom is checked, uncheck "None of the above"
        val otherSymptomCheckboxes = listOf(
            binding.cbSymptomBleeding,
            binding.cbSymptomHeadacheBlurredVision,
            binding.cbSymptomSwelling,
            binding.cbSymptomFeverChills,
            binding.cbSymptomReducedMovements,
            binding.cbSymptomSevereAbdominalPain
        )

        otherSymptomCheckboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.cbSymptomNone.isChecked = false
                }
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveFollowUpAnc.setOnClickListener {
            if (validateForm()) {
                saveFollowUpAncVisit()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Clear previous errors
        binding.tilVisitDate.error = null
        binding.tilVisitNumber.error = null
        binding.tilBpSystolic.error = null
        binding.tilBpDiastolic.error = null
        binding.tilReferralReason.error = null

        // Validate Visit Date
        if (binding.etVisitDate.text.isNullOrBlank()) {
            binding.tilVisitDate.error = "Required"
            isValid = false
        }

        // Validate Visit Number
        if (binding.etVisitNumber.text.isNullOrBlank()) {
            binding.tilVisitNumber.error = "Required"
            isValid = false
        }

        // Validate Facility Type
        if (binding.rgFacilityType.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Please select facility type", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validate BP fields if switch is ON
        if (binding.switchBpRecorded.isChecked) {
            if (binding.etBpSystolic.text.isNullOrBlank()) {
                binding.tilBpSystolic.error = "Required when BP recorded"
                isValid = false
            }
            if (binding.etBpDiastolic.text.isNullOrBlank()) {
                binding.tilBpDiastolic.error = "Required when BP recorded"
                isValid = false
            }
        }

        // Validate Referral Reason if switch is ON
        if (binding.switchReferralMade.isChecked) {
            if (binding.etReferralReason.text.isNullOrBlank()) {
                binding.tilReferralReason.error = "Required when referral made"
                isValid = false
            }
        }

        if (!isValid) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }

    private fun observeViewModel() {
        viewModel.isSaving.observe(this) { saving ->
            binding.btnSaveFollowUpAnc.isEnabled = !saving
            binding.btnSaveFollowUpAnc.text = if (saving) "Saving..." else "Save ANC Follow-up"
        }

        // Local DB save result
        viewModel.saveSuccess.observe(this) { success ->
            if (success == true) {
                // Local save is done; don't finish here because we also submit to backend
                Toast.makeText(this, "ANC follow-up saved locally", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // 🔥 Backend submit result (POST survey/anc-followup)
        viewModel.submitResult.observe(this) { state ->
            when (state) {
                is ResultState.Idle -> {
                    // Do nothing
                }

                is ResultState.Loading -> {
                    Toast.makeText(this, "Submitting ANC follow-up...", Toast.LENGTH_SHORT).show()
                }

                is ResultState.Success -> {
                    Toast.makeText(this, state.data.message, Toast.LENGTH_LONG).show()
                    Log.d("ANC_FOLLOWUP", "Backend response: followupId = ${state.data.followupId}")
                    finish() // Close only on backend success
                }

                is ResultState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    // Local save already done; ASHA can retry sync later
                }
            }
        }
    }

    private fun saveFollowUpAncVisit() {
        val currentPatientId = patientId
        val currentPregnancyId = pregnancyId

        if (currentPatientId.isNullOrBlank()) {
            Toast.makeText(this, "No patient ID available", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentPregnancyId.isNullOrBlank()) {
            Toast.makeText(this, "No pregnancy ID available", Toast.LENGTH_SHORT).show()
            return
        }

        // Build entity from form using helper
        val entity = AncVisitFormMapper.buildEntityFromForm(binding, currentPregnancyId)

        // Log entity to verify mapping
        Log.d("ANC_FOLLOWUP", "Entity = $entity")

        // Map to backend request (POST survey/anc-followup)
        val request = entity.toAncFollowUpRequest(currentPatientId)
        Log.d("ANC_FOLLOWUP", "Mapped request = $request")

        // Save locally (offline-first)
        viewModel.saveVisit(entity)

        // Submit to backend
        viewModel.submitAncFollowUp(request)
    }
}

