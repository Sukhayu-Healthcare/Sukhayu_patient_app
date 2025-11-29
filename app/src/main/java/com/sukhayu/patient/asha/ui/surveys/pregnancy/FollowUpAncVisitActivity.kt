package com.sukhayu.patient.asha.ui.surveys.pregnancy

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.repository.AncVisitRepository
import com.sukhayu.patient.databinding.ActivityFollowUpAncVisitBinding
import java.text.SimpleDateFormat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowUpAncVisitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "Follow-up ANC Visit"
            setDisplayHomeAsUpEnabled(true)
        }

        // Initialize ViewModel
        val dao = AshaLocalDatabase.getInstance(this).ancVisitDao()
        val repository = AncVisitRepository(dao)
        val factory = FollowUpAncVisitViewModelFactory(repository)
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
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Follow-up visit saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveFollowUpAncVisit() {
        val currentPregnancyId = pregnancyId
        if (currentPregnancyId.isNullOrBlank()) {
            Toast.makeText(this, "No pregnancy/patient ID available", Toast.LENGTH_SHORT).show()
            return
        }

        // Build entity from form using helper
        val entity = AncVisitFormMapper.buildEntityFromForm(binding, currentPregnancyId)

        // Save via ViewModel
        viewModel.saveVisit(entity)
    }
}

