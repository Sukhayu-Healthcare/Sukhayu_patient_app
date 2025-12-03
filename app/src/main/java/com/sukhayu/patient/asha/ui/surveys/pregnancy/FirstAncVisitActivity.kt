package com.sukhayu.patient.asha.ui.surveys.pregnancy

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.asha.ui.surveys.tb.ResultState
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.PregnancyEntity
import com.sukhayu.patient.data.local.entity.toFirstAncVisitRequest
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.repository.PregnancyRepository
import com.sukhayu.patient.databinding.ActivityFirstAncVisitBinding
import com.sukhayu.utils.VoiceInputHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class FirstAncVisitActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PATIENT_ID = "patient_id"
        const val EXTRA_PATIENT_NAME = "patient_name"
        const val EXTRA_PATIENT_PHONE = "patient_phone"
        const val EXTRA_PATIENT_GENDER = "patient_gender"
        const val EXTRA_PATIENT_WEIGHT = "patient_weight"
    }

    private lateinit var binding: ActivityFirstAncVisitBinding
    private lateinit var viewModel: FirstAncVisitViewModel
    private var patientId: String? = null
    private var patientName: String? = null
    private var patientPhone: String? = null

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstAncVisitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "First ANC Visit"
            setDisplayHomeAsUpEnabled(true)
        }

        // Initialize ViewModel
        val dao = AshaLocalDatabase.getInstance(this).pregnancyDao()
        val apiService = ApiClient.retrofit
        val repository = PregnancyRepository(dao, apiService)
        val factory = FirstAncVisitViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FirstAncVisitViewModel::class.java]

        // 1) Read Intent extras and fill header
        readIntentExtrasAndFillHeader()

        // 2) Hook up date pickers
        setupDatePickers()

        // 3) Setup conditional visibility for "Other" fields
        setupConditionalFields()

        // 4) Observe ViewModel state
        observeViewModel()

        // 5) Basic validation on Save
        setupSaveButton()

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun observeViewModel() {
        viewModel.isSaving.observe(this) { saving ->
            binding.btnSaveFirstAnc.isEnabled = !saving
        }

        // Local DB save result
        viewModel.saveSuccess.observe(this) { success ->
            if (success == true) {
                // Local save is done; don't finish here because we also submit to backend
                Toast.makeText(this, "First ANC Visit saved locally", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // 🔥 Backend submit result (POST survey/anc)
        viewModel.submitResult.observe(this) { state ->
            when (state) {
                is ResultState.Idle -> Unit
                is ResultState.Loading -> {
                    Toast.makeText(this, "Submitting First ANC Visit...", Toast.LENGTH_SHORT).show()
                }
                is ResultState.Success -> {
                    Toast.makeText(this, state.data.message, Toast.LENGTH_LONG).show()
                    Log.d("FIRST_ANC", "ANC ID from backend: ${state.data.ancId}")
                    // Close screen only after backend confirms success
                    finish()
                }
                is ResultState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    // Local data is already saved; ASHA can retry sync later
                }
            }
        }
    }

    private fun readIntentExtrasAndFillHeader() {
        patientId = intent.getStringExtra(EXTRA_PATIENT_ID)
        patientName = intent.getStringExtra(EXTRA_PATIENT_NAME)
        patientPhone = intent.getStringExtra(EXTRA_PATIENT_PHONE)
        val patientGender = intent.getStringExtra(EXTRA_PATIENT_GENDER)
        val patientWeight = intent.getStringExtra(EXTRA_PATIENT_WEIGHT)

        binding.tvPatientNameHeader.text = "Name: ${patientName ?: "-"}"
        binding.tvPatientPhoneHeader.text = "Phone: ${patientPhone ?: "-"}"
        binding.tvPatientGenderHeader.text = "Gender: ${patientGender ?: "-"}"
        binding.tvPatientWeightHeader.text = "Weight: ${patientWeight ?: "-"}"
    }

    private fun setupDatePickers() {
        // LMP Date - with auto-calculation of EDD
        binding.etLmpDate.setOnClickListener {
            showDatePicker { date ->
                binding.etLmpDate.setText(dateFormat.format(date))
                // Auto-calculate EDD as LMP + 280 days (40 weeks)
                val eddCalendar = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.DAY_OF_YEAR, 280)
                }
                binding.etEddDate.setText(dateFormat.format(eddCalendar.time))
            }
        }

        // EDD Date
        binding.etEddDate.setOnClickListener {
            showDatePicker { date ->
                binding.etEddDate.setText(dateFormat.format(date))
            }
        }

        // First ANC Visit Date - default to today
        binding.etFirstAncDate.setOnClickListener {
            showDatePicker { date ->
                binding.etFirstAncDate.setText(dateFormat.format(date))
            }
        }
        // Set today's date as default
        binding.etFirstAncDate.setText(dateFormat.format(Date()))

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

    private fun setupConditionalFields() {
        // Show/hide complication note when switch is toggled
        binding.switchPreviousComplication.setOnCheckedChangeListener { _, isChecked ->
            binding.tilPreviousComplicationNote.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Show/hide "Other" illness field when "Other" chip is checked
        binding.chipIllnessOther.setOnCheckedChangeListener { _, isChecked ->
            binding.tilSeriousIllnessOther.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveFirstAnc.setOnClickListener {
            if (validateForm()) {
                saveFirstAncVisit()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Clear previous errors
        binding.tilLmpDate.error = null
        binding.tilGravida.error = null
        binding.tilPara.error = null
        binding.tilFirstAncDate.error = null

        // Validate LMP date
        if (binding.etLmpDate.text.isNullOrBlank()) {
            binding.tilLmpDate.error = "Required"
            isValid = false
        }

        // Validate Gravida
        if (binding.etGravida.text.isNullOrBlank()) {
            binding.tilGravida.error = "Required"
            isValid = false
        }

        // Validate Para
        if (binding.etPara.text.isNullOrBlank()) {
            binding.tilPara.error = "Required"
            isValid = false
        }

        // Validate First ANC visit date
        if (binding.etFirstAncDate.text.isNullOrBlank()) {
            binding.tilFirstAncDate.error = "Required"
            isValid = false
        }

        // Validate ANC place radio group
        if (binding.rgAncPlace.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Please select place of ANC care", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validate Delivery place radio group
        if (binding.rgDeliveryPlace.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Please select planned place of delivery", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }

    private fun saveFirstAncVisit() {
        // Build PregnancyEntity from form fields
        val entity = PregnancyEntity(
            womanId = patientId ?: "",
            lmpDate = binding.etLmpDate.text.toString(),
            eddDate = binding.etEddDate.text.toString(),
            gravida = binding.etGravida.text.toString().toIntOrNull(),
            para = binding.etPara.text.toString().toIntOrNull(),
            livingChildren = binding.etLivingChildren.text.toString().toIntOrNull(),
            hasPreviousComplication = binding.switchPreviousComplication.isChecked,
            previousComplicationNote = if (binding.switchPreviousComplication.isChecked) {
                binding.etPreviousComplicationNote.text.toString()
            } else null,
            hasSevereBleeding = binding.switchSevereBleedingNow.isChecked,
            hasConvulsionsHistory = binding.switchConvulsionsHistory.isChecked,
            hasHighBpHistory = binding.switchHighBpHistory.isChecked,
            seriousIllnesses = collectRiskChips(),
            firstAncDate = binding.etFirstAncDate.text.toString(),
            ancPlace = selectedAncPlace(),
            deliveryPlace = selectedDeliveryPlace(),
            dangerSignsExplained = binding.switchDangerSignsExplained.isChecked,
            nextVisitDate = binding.etNextVisitDate.text.toString().ifBlank { null }
        )

        // Log entity to verify mapping
        Log.d("FIRST_ANC", "Entity = $entity")

        // Map to backend request (POST survey/anc)
        val request = entity.toFirstAncVisitRequest()
        Log.d("FIRST_ANC", "Mapped request = $request")

        // Save locally (offline-first)
        viewModel.savePregnancy(entity)

        // Submit to backend
        viewModel.submitFirstAncVisit(request)
    }

    /**
     * Collect selected illness chips into a comma-separated string.
     */
    private fun collectRiskChips(): String {
        val selectedIllnesses = mutableListOf<String>()

        if (binding.chipIllnessDiabetes.isChecked) selectedIllnesses.add("Diabetes")
        if (binding.chipIllnessHighBp.isChecked) selectedIllnesses.add("High BP")
        if (binding.chipIllnessHeartDisease.isChecked) selectedIllnesses.add("Heart disease")
        if (binding.chipIllnessTb.isChecked) selectedIllnesses.add("TB")
        if (binding.chipIllnessHiv.isChecked) selectedIllnesses.add("HIV")
        if (binding.chipIllnessOther.isChecked) {
            val otherIllness = binding.etSeriousIllnessOther.text.toString()
            if (otherIllness.isNotBlank()) {
                selectedIllnesses.add("Other: $otherIllness")
            }
        }

        return selectedIllnesses.joinToString(", ")
    }

    /**
     * Get selected ANC place from radio group.
     */
    private fun selectedAncPlace(): String {
        return when (binding.rgAncPlace.checkedRadioButtonId) {
            binding.rbAncPlaceGovt.id -> "GOVT"
            binding.rbAncPlacePrivate.id -> "PRIVATE"
            binding.rbAncPlaceNotDecided.id -> "NOT_DECIDED"
            else -> "NOT_DECIDED"
        }
    }

    /**
     * Get selected delivery place from radio group.
     */
    private fun selectedDeliveryPlace(): String {
        return when (binding.rgDeliveryPlace.checkedRadioButtonId) {
            binding.rbDeliveryPlaceGovt.id -> "GOVT"
            binding.rbDeliveryPlacePrivate.id -> "PRIVATE"
            binding.rbDeliveryPlaceHome.id -> "HOME"
            binding.rbDeliveryPlaceNotDecided.id -> "NOT_DECIDED"
            else -> "NOT_DECIDED"
        }
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

