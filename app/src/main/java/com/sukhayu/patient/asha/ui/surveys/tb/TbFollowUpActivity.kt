package com.sukhayu.patient.asha.ui.surveys.tb

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.TbFollowUpEntity
import com.sukhayu.patient.data.repository.TbFollowUpRepository
import com.sukhayu.patient.databinding.ActivityTbFollowUpBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * TB Treatment Follow-up (DOTS) Activity
 * Template ID: "tb_follow_up_template"
 *
 * Tracks directly observed treatment (DOTS) follow-up visits for TB patients
 * during intensive and continuation phases of treatment.
 */
class TbFollowUpActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PATIENT_ID = "patient_id"
        const val EXTRA_PATIENT_NAME = "patient_name"
        const val EXTRA_PATIENT_PHONE = "patient_phone"
        const val EXTRA_PATIENT_GENDER = "patient_gender"
        const val EXTRA_PATIENT_WEIGHT = "patient_weight"
        const val TEMPLATE_ID = "tb_follow_up_template"
    }

    private lateinit var binding: ActivityTbFollowUpBinding
    private lateinit var viewModel: TbFollowUpViewModel
    private var patientId: String? = null

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTbFollowUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "TB Treatment Follow-up (DOTS)"
            setDisplayHomeAsUpEnabled(true)
        }

        // Initialize ViewModel
        val dao = AshaLocalDatabase.getInstance(this).tbFollowUpDao()
        val repository = TbFollowUpRepository(dao)
        val factory = TbFollowUpViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TbFollowUpViewModel::class.java]

        // Read Intent extras and pre-fill patient data
        readIntentExtrasAndPrefillForm()

        // Setup spinners
        setupSpinners()

        // Setup date pickers
        setupDatePickers()

        // Setup conditional visibility
        setupConditionalFields()

        // Observe ViewModel state
        observeViewModel()

        // Setup Save button
        setupSaveButton()
    }

    private fun observeViewModel() {
        viewModel.isSaving.observe(this) { saving ->
            binding.btnSaveTbFollowUp.isEnabled = !saving
            binding.btnSaveTbFollowUp.text = if (saving) "Saving..." else "Save TB Follow-up"
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success == true) {
                Toast.makeText(this, "TB Follow-up saved successfully", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun readIntentExtrasAndPrefillForm() {
        patientId = intent.getStringExtra(EXTRA_PATIENT_ID)
        val patientName = intent.getStringExtra(EXTRA_PATIENT_NAME)
        val patientPhone = intent.getStringExtra(EXTRA_PATIENT_PHONE)
        val patientGender = intent.getStringExtra(EXTRA_PATIENT_GENDER)
        val patientWeight = intent.getStringExtra(EXTRA_PATIENT_WEIGHT)

        // Display patient details in header
        binding.tvPatientNameHeader.text = "Name: ${patientName ?: "-"}"
        binding.tvPatientPhoneHeader.text = "Phone: ${patientPhone ?: "-"}"
        binding.tvPatientGenderHeader.text = "Gender: ${patientGender ?: "-"}"
        binding.tvPatientWeightHeader.text = "Weight: ${patientWeight ?: "-"}"
    }

    private fun setupSpinners() {
        // Phase of Treatment spinner
        val phases = arrayOf("Intensive", "Continuation")
        val phaseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, phases)
        phaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPhaseOfTreatment.adapter = phaseAdapter

        // Visit Type spinner
        val visitTypes = arrayOf("Home visit", "Facility visit")
        val visitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, visitTypes)
        visitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVisitType.adapter = visitAdapter

        // DOT Provider spinner
        val dotProviders = arrayOf("ASHA", "Family member", "Health worker", "Other")
        val dotAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dotProviders)
        dotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDotProvider.adapter = dotAdapter
    }

    private fun setupDatePickers() {
        // Visit date - default to today
        binding.etVisitDate.setOnClickListener {
            showDatePicker { date ->
                binding.etVisitDate.setText(dateFormat.format(date))
            }
        }
        binding.etVisitDate.setText(dateFormat.format(Date()))

        // Next follow-up date
        binding.etNextFollowUpDate.setOnClickListener {
            showDatePicker { date ->
                binding.etNextFollowUpDate.setText(dateFormat.format(date))
            }
        }
    }

    private fun setupConditionalFields() {
        // Show referral reason only if referred for side effects
        binding.switchReferredForSideEffects.setOnCheckedChangeListener { _, isChecked ->
            binding.tilReferralReason.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                binding.etReferralReason.text?.clear()
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveTbFollowUp.setOnClickListener {
            if (validateForm()) {
                saveTbFollowUp()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate TB patient ID
        if (binding.etTbPatientId.text.isNullOrBlank()) {
            binding.tilTbPatientId.error = "Required"
            isValid = false
        } else {
            binding.tilTbPatientId.error = null
        }

        // Validate visit date
        if (binding.etVisitDate.text.isNullOrBlank()) {
            binding.tilVisitDate.error = "Required"
            isValid = false
        } else {
            binding.tilVisitDate.error = null
        }

        // Validate doses missed
        if (binding.etDosesMissed.text.isNullOrBlank()) {
            binding.tilDosesMissed.error = "Required (enter 0 if none)"
            isValid = false
        } else {
            binding.tilDosesMissed.error = null
        }

        // Validate weight
        if (binding.etWeightKg.text.isNullOrBlank()) {
            binding.tilWeightKg.error = "Required"
            isValid = false
        } else {
            binding.tilWeightKg.error = null
        }

        // Validate next follow-up date
        if (binding.etNextFollowUpDate.text.isNullOrBlank()) {
            binding.tilNextFollowUpDate.error = "Required"
            isValid = false
        } else {
            binding.tilNextFollowUpDate.error = null
        }

        // Validate referral reason if referred
        if (binding.switchReferredForSideEffects.isChecked && binding.etReferralReason.text.isNullOrBlank()) {
            binding.tilReferralReason.error = "Required when referred"
            isValid = false
        } else {
            binding.tilReferralReason.error = null
        }

        if (!isValid) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }

    private fun saveTbFollowUp() {
        val entity = TbFollowUpEntity(
            patientId = patientId ?: "",
            templateId = TEMPLATE_ID,

            // a) Visit details
            tbPatientIdOrNikshay = binding.etTbPatientId.text.toString(),
            visitDate = binding.etVisitDate.text.toString(),
            phaseOfTreatment = binding.spinnerPhaseOfTreatment.selectedItem.toString(),
            visitType = binding.spinnerVisitType.selectedItem.toString(),

            // b) Adherence and symptoms
            dosesMissedSinceLastVisit = binding.etDosesMissed.text.toString().toIntOrNull() ?: 0,
            vomitingAfterMedicines = binding.switchVomiting.isChecked,
            yellowEyesOrSkin = binding.switchYellowEyes.isChecked,
            severeSkinRashOrItching = binding.switchSkinRash.isChecked,
            jointPain = binding.switchJointPain.isChecked,
            persistentCoughOrBreathlessness = binding.switchCough.isChecked,
            feverLastWeek = binding.switchFever.isChecked,
            weightKg = binding.etWeightKg.text.toString().toDoubleOrNull() ?: 0.0,

            // c) Programmatic details
            dotProvider = binding.spinnerDotProvider.selectedItem.toString(),
            drugBoxCheckedAndConsistent = binding.switchDrugBoxChecked.isChecked,
            counsellingGiven = binding.switchCounselling.isChecked,

            // d) Decision / action
            treatmentContinuedAsPlanned = binding.switchTreatmentContinued.isChecked,
            referredForSideEffects = binding.switchReferredForSideEffects.isChecked,
            referralReason = if (binding.switchReferredForSideEffects.isChecked) {
                binding.etReferralReason.text.toString()
            } else null,
            nextFollowUpDate = binding.etNextFollowUpDate.text.toString()
        )

        viewModel.saveTbFollowUp(entity)
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

