package com.sukhayu.patient.asha.ui.surveys.general_survey

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.entity.GeneralSurveyEntity
import com.sukhayu.utils.VoiceInputHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Canonical General Survey Activity.
 * - Package must be: com.sukhayu.patient.asha.ui.surveys.general_survey
 * - File name must be: GeneralSurveyActivity.kt
 * - Class name must be: GeneralSurveyActivity
 *
 * Renders General Health Survey form for community screening
 */
class GeneralSurveyActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PATIENT_ID = "patient_id"
        const val EXTRA_PATIENT_NAME = "patient_name"
        const val EXTRA_PATIENT_PHONE = "patient_phone"
        const val EXTRA_PATIENT_GENDER = "patient_gender"
        const val EXTRA_PATIENT_AGE = "patient_age"
        const val TEMPLATE_ID = "general_survey_template"
    }

    private lateinit var voiceHelper: VoiceInputHelper
    private lateinit var viewModel: GeneralSurveyViewModel
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var patientId: String? = null
    private var patientName: String? = null

    // Section 1: Identification
    private lateinit var etVisitDate: TextInputEditText
    private lateinit var etLocation: TextInputEditText

    // Section 2: Existing Conditions
    private lateinit var rgDiabetes: RadioGroup
    private lateinit var rgHypertension: RadioGroup
    private lateinit var rgHeartDisease: RadioGroup
    private lateinit var rgStroke: RadioGroup
    private lateinit var rgKidneyDisease: RadioGroup
    private lateinit var etOtherConditions: TextInputEditText

    // Section 3: Symptoms
    private lateinit var rgFrequentUrination: RadioGroup
    private lateinit var rgExcessiveThirst: RadioGroup
    private lateinit var rgWeightLoss: RadioGroup
    private lateinit var rgBlurredVision: RadioGroup
    private lateinit var rgChestPain: RadioGroup
    private lateinit var rgShortnessOfBreath: RadioGroup
    private lateinit var rgFatigue: RadioGroup

    // Section 4: Risk Factors
    private lateinit var rgFamilyHistory: RadioGroup
    private lateinit var rgTobaccoUse: RadioGroup
    private lateinit var rgAlcoholUse: RadioGroup
    private lateinit var rgPhysicalActivity: RadioGroup
    private lateinit var rgUnhealthyDiet: RadioGroup

    // Section 5: Service Use
    private lateinit var rgRegularCheckups: RadioGroup
    private lateinit var rgCurrentMedication: RadioGroup
    private lateinit var tilMedicationDetails: TextInputLayout
    private lateinit var etMedicationDetails: TextInputEditText
    private lateinit var rgRecentBPCheck: RadioGroup
    private lateinit var rgRecentSugarCheck: RadioGroup

    // Section 6: ASHA Assessment
    private lateinit var rgReferralNeeded: RadioGroup
    private lateinit var tvReferralFacilityLabel: TextView
    private lateinit var rgReferralFacility: RadioGroup
    private lateinit var etRemarks: TextInputEditText

    // Patient header
    private lateinit var tvPatientNameHeader: TextView
    private lateinit var tvPatientPhoneHeader: TextView
    private lateinit var tvPatientGenderHeader: TextView
    private lateinit var tvPatientAgeHeader: TextView

    // Save button
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_survey)

        supportActionBar?.apply {
            title = "General Health Survey"
            setDisplayHomeAsUpEnabled(true)
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[GeneralSurveyViewModel::class.java]

        initializeViews()
        readIntentExtrasAndPrefillForm()
        setupDatePickers()
        setupConditionalFields()
        setupSaveButton()

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun initializeViews() {
        // Patient Header
        tvPatientNameHeader = findViewById(R.id.tvPatientNameHeader)
        tvPatientPhoneHeader = findViewById(R.id.tvPatientPhoneHeader)
        tvPatientGenderHeader = findViewById(R.id.tvPatientGenderHeader)
        tvPatientAgeHeader = findViewById(R.id.tvPatientAgeHeader)

        // Section 1: Identification
        etVisitDate = findViewById(R.id.etVisitDate)
        etLocation = findViewById(R.id.etLocation)

        // Section 2: Existing Conditions
        rgDiabetes = findViewById(R.id.rgDiabetes)
        rgHypertension = findViewById(R.id.rgHypertension)
        rgHeartDisease = findViewById(R.id.rgHeartDisease)
        rgStroke = findViewById(R.id.rgStroke)
        rgKidneyDisease = findViewById(R.id.rgKidneyDisease)
        etOtherConditions = findViewById(R.id.etOtherConditions)

        // Section 3: Symptoms
        rgFrequentUrination = findViewById(R.id.rgFrequentUrination)
        rgExcessiveThirst = findViewById(R.id.rgExcessiveThirst)
        rgWeightLoss = findViewById(R.id.rgWeightLoss)
        rgBlurredVision = findViewById(R.id.rgBlurredVision)
        rgChestPain = findViewById(R.id.rgChestPain)
        rgShortnessOfBreath = findViewById(R.id.rgShortnessOfBreath)
        rgFatigue = findViewById(R.id.rgFatigue)

        // Section 4: Risk Factors
        rgFamilyHistory = findViewById(R.id.rgFamilyHistory)
        rgTobaccoUse = findViewById(R.id.rgTobaccoUse)
        rgAlcoholUse = findViewById(R.id.rgAlcoholUse)
        rgPhysicalActivity = findViewById(R.id.rgPhysicalActivity)
        rgUnhealthyDiet = findViewById(R.id.rgUnhealthyDiet)

        // Section 5: Service Use
        rgRegularCheckups = findViewById(R.id.rgRegularCheckups)
        rgCurrentMedication = findViewById(R.id.rgCurrentMedication)
        tilMedicationDetails = findViewById(R.id.tilMedicationDetails)
        etMedicationDetails = findViewById(R.id.etMedicationDetails)
        rgRecentBPCheck = findViewById(R.id.rgRecentBPCheck)
        rgRecentSugarCheck = findViewById(R.id.rgRecentSugarCheck)

        // Section 6: ASHA Assessment
        rgReferralNeeded = findViewById(R.id.rgReferralNeeded)
        tvReferralFacilityLabel = findViewById(R.id.tvReferralFacilityLabel)
        rgReferralFacility = findViewById(R.id.rgReferralFacility)
        etRemarks = findViewById(R.id.etRemarks)

        // Save button
        btnSave = findViewById(R.id.btnSaveGeneralSurvey)
    }

    private fun readIntentExtrasAndPrefillForm() {
        patientId = intent.getStringExtra(EXTRA_PATIENT_ID)

        // Validate that patientId is not null or empty
        if (patientId.isNullOrBlank()) {
            Toast.makeText(this, "Error: No patient selected. Please search and select a patient first.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        patientName = intent.getStringExtra(EXTRA_PATIENT_NAME)
        val patientPhone = intent.getStringExtra(EXTRA_PATIENT_PHONE)
        val patientGender = intent.getStringExtra(EXTRA_PATIENT_GENDER)
        val patientAge = intent.getStringExtra(EXTRA_PATIENT_AGE)

        // Display patient details in header
        tvPatientNameHeader.text = "Name: ${patientName ?: "-"}"
        tvPatientPhoneHeader.text = "Phone: ${patientPhone ?: "-"}"
        tvPatientGenderHeader.text = "Gender: ${patientGender ?: "-"}"
        tvPatientAgeHeader.text = "Age: ${patientAge ?: "-"}"

        // Pre-fill visit date with today's date
        etVisitDate.setText(dateFormat.format(Date()))
    }

    private fun setupDatePickers() {
        etVisitDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    etVisitDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupConditionalFields() {
        // Show medication details when "Yes" is selected
        rgCurrentMedication.setOnCheckedChangeListener { _, checkedId ->
            tilMedicationDetails.visibility = if (checkedId == R.id.rbCurrentMedicationYes) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        // Show referral facility options when referral is needed
        rgReferralNeeded.setOnCheckedChangeListener { _, checkedId ->
            val isReferralNeeded = checkedId == R.id.rbReferralNeededYes
            tvReferralFacilityLabel.visibility = if (isReferralNeeded) View.VISIBLE else View.GONE
            rgReferralFacility.visibility = if (isReferralNeeded) View.VISIBLE else View.GONE
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            if (validateForm()) {
                saveGeneralSurvey()
            }
        }
    }

    private fun validateForm(): Boolean {
        val visitDate = etVisitDate.text.toString().trim()

        if (visitDate.isEmpty()) {
            Toast.makeText(this, "Please select visit date", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if at least one section has data
        if (!hasAnyData()) {
            Toast.makeText(this, "Please fill at least some survey information", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun hasAnyData(): Boolean {
        // Check if any radio group has a selection
        return rgDiabetes.checkedRadioButtonId != -1 ||
                rgHypertension.checkedRadioButtonId != -1 ||
                rgHeartDisease.checkedRadioButtonId != -1 ||
                rgFrequentUrination.checkedRadioButtonId != -1 ||
                rgFamilyHistory.checkedRadioButtonId != -1 ||
                rgTobaccoUse.checkedRadioButtonId != -1 ||
                etOtherConditions.text.toString().isNotEmpty() ||
                etRemarks.text.toString().isNotEmpty()
    }

    private fun saveGeneralSurvey() {
        Log.d("GENERAL_SURVEY_DB", "saveGeneralSurvey called")

        // Ensure patientId is available
        if (patientId.isNullOrBlank()) {
            Toast.makeText(this, "Error: Patient ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Build GeneralSurveyEntity from form data
        val surveyEntity = GeneralSurveyEntity(
            patientId = patientId!!,
            patientName = patientName,

            // Section 1: Identification
            visitDate = etVisitDate.text.toString().trim(),
            location = etLocation.text.toString().trim().takeIf { it.isNotEmpty() },

            // Section 2: Existing Conditions
            hasDiabetes = getRadioGroupValue(rgDiabetes),
            hasHypertension = getRadioGroupValue(rgHypertension),
            hasHeartDisease = getRadioGroupValue(rgHeartDisease),
            hasStroke = getRadioGroupValue(rgStroke),
            hasKidneyDisease = getRadioGroupValue(rgKidneyDisease),
            otherConditions = etOtherConditions.text.toString().trim().takeIf { it.isNotEmpty() },

            // Section 3: Symptoms
            symptomFrequentUrination = getRadioGroupValue(rgFrequentUrination),
            symptomExcessiveThirst = getRadioGroupValue(rgExcessiveThirst),
            symptomWeightLoss = getRadioGroupValue(rgWeightLoss),
            symptomBlurredVision = getRadioGroupValue(rgBlurredVision),
            symptomChestPain = getRadioGroupValue(rgChestPain),
            symptomShortnessOfBreath = getRadioGroupValue(rgShortnessOfBreath),
            symptomFatigue = getRadioGroupValue(rgFatigue),

            // Section 4: Risk Factors
            riskFamilyHistory = getRadioGroupValue(rgFamilyHistory),
            riskTobaccoUse = getRadioGroupValue(rgTobaccoUse),
            riskAlcoholUse = getRadioGroupValue(rgAlcoholUse),
            riskPhysicalInactivity = getRadioGroupValue(rgPhysicalActivity),
            riskUnhealthyDiet = getRadioGroupValue(rgUnhealthyDiet),

            // Section 5: Service Use
            hasRegularCheckups = getRadioGroupValue(rgRegularCheckups),
            onCurrentMedication = getRadioGroupValue(rgCurrentMedication),
            medicationDetails = etMedicationDetails.text.toString().trim().takeIf { it.isNotEmpty() },
            hadRecentBpCheck = getRadioGroupValue(rgRecentBPCheck),
            hadRecentSugarCheck = getRadioGroupValue(rgRecentSugarCheck),

            // Section 6: ASHA Assessment
            referralNeeded = getRadioGroupValue(rgReferralNeeded),
            referralFacility = getReferralFacilityValue(),
            remarks = etRemarks.text.toString().trim().takeIf { it.isNotEmpty() }
        )

        Log.d("GENERAL_SURVEY_DB", "Survey entity created: $surveyEntity")

        // Save using ViewModel
        viewModel.saveSurvey(
            survey = surveyEntity,
            onSuccess = { rowId ->
                Log.d("GENERAL_SURVEY_DB", "Successfully saved with ID: $rowId")
                runOnUiThread {
                    Toast.makeText(this, "General Survey saved successfully (ID: $rowId)", Toast.LENGTH_LONG).show()
                    finish()
                }
            },
            onError = { error ->
                Log.e("GENERAL_SURVEY_DB", "Error saving survey", error)
                runOnUiThread {
                    Toast.makeText(this, "Error saving survey: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    /**
     * Helper method to get Boolean value from RadioGroup
     * Returns true if "Yes" is selected, false if "No" is selected, null if nothing selected
     */
    private fun getRadioGroupValue(radioGroup: RadioGroup): Boolean? {
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId == -1) return null

        val selectedButton = findViewById<RadioButton>(selectedId)
        val selectedText = selectedButton?.text?.toString() ?: return null

        return selectedText.equals("Yes", ignoreCase = true)
    }

    /**
     * Helper method to get referral facility value
     */
    private fun getReferralFacilityValue(): String? {
        val selectedId = rgReferralFacility.checkedRadioButtonId
        if (selectedId == -1) return null

        val selectedButton = findViewById<RadioButton>(selectedId)
        return selectedButton?.text?.toString()
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

