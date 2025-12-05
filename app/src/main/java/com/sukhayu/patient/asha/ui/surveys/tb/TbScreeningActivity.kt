package com.sukhayu.patient.asha.ui.surveys.tb

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.entity.TbScreeningEntity
import com.sukhayu.patient.databinding.ActivityTbScreeningBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale

/**
 * TB Screening Activity - Template ID: "tb_screening_template"
 */
class TbScreeningActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PATIENT_ID = "patient_id"
        const val EXTRA_PATIENT_NAME = "patient_name"
        const val EXTRA_PATIENT_PHONE = "patient_phone"
        const val EXTRA_PATIENT_GENDER = "patient_gender"
        const val EXTRA_PATIENT_WEIGHT = "patient_weight"
        const val TEMPLATE_ID = "tb_screening_template"
    }

    private lateinit var binding: ActivityTbScreeningBinding
    private lateinit var viewModel: TbScreeningViewModel
    private var patientId: String? = null

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTbScreeningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "TB Screening / Suspect Form"
            setDisplayHomeAsUpEnabled(true)
        }

        viewModel = ViewModelProvider(this)[TbScreeningViewModel::class.java]

        readIntentExtrasAndPrefillForm()
        setupDatePickers()
        setupConditionalFields()
        observeViewModel()
        setupSaveButton()
    }

    private fun observeViewModel() {
        viewModel.isSaving.observe(this) { saving ->
            binding.btnSaveTbScreening.isEnabled = !saving
            binding.btnSaveTbScreening.text =
                if (saving) "Saving..." else "Save TB Screening"
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

        binding.tvPatientNameHeader.text = "Name: ${patientName ?: "-"}"
        binding.tvPatientPhoneHeader.text = "Phone: ${patientPhone ?: "-"}"
        binding.tvPatientGenderHeader.text = "Gender: ${patientGender ?: "-"}"
        binding.tvPatientWeightHeader.text = "Weight: ${patientWeight ?: "-"}"

        binding.etName.setText(patientName ?: "")
        binding.etMobileNumber.setText(patientPhone ?: "")

        when (patientGender) {
            "Male" -> binding.rgSex.check(R.id.rbSexM)
            "Female" -> binding.rgSex.check(R.id.rbSexF)
            else -> binding.rgSex.clearCheck()
        }
    }

    private fun setupDatePickers() {
        binding.etDateOfScreening.setOnClickListener {
            showDatePicker { date ->
                binding.etDateOfScreening.setText(dateFormat.format(date))
            }
        }
        binding.etDateOfScreening.setText(dateFormat.format(Date()))

        binding.etSputumCollectionDate.setOnClickListener {
            showDatePicker { date ->
                binding.etSputumCollectionDate.setText(dateFormat.format(date))
            }
        }
    }

    private fun setupConditionalFields() {
        binding.switchSputumCollected.setOnCheckedChangeListener { _, isChecked ->
            binding.tilSputumCollectionDate.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                binding.etSputumCollectionDate.text?.clear()
            }
        }

        binding.switchReferredToHigherCentre.setOnCheckedChangeListener { _, isChecked ->
            binding.tilReferralPlaceName.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                binding.etReferralPlaceName.text?.clear()
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveTbScreening.setOnClickListener {
            if (validateForm()) {
                saveTbScreening()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.etName.text.isNullOrBlank()) {
            binding.tilName.error = "Required"
            isValid = false
        } else binding.tilName.error = null

        if (binding.etAgeYears.text.isNullOrBlank()) {
            binding.tilAgeYears.error = "Required"
            isValid = false
        } else binding.tilAgeYears.error = null

        if (binding.rgSex.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Please select sex (M/F/O)", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (binding.etAddressVillage.text.isNullOrBlank()) {
            binding.tilAddressVillage.error = "Required"
            isValid = false
        } else binding.tilAddressVillage.error = null

        if (binding.etAshaIdOrName.text.isNullOrBlank()) {
            binding.tilAshaIdOrName.error = "Required"
            isValid = false
        } else binding.tilAshaIdOrName.error = null

        if (binding.etDateOfScreening.text.isNullOrBlank()) {
            binding.tilDateOfScreening.error = "Required"
            isValid = false
        } else binding.tilDateOfScreening.error = null

        if (binding.switchSputumCollected.isChecked &&
            binding.etSputumCollectionDate.text.isNullOrBlank()
        ) {
            binding.tilSputumCollectionDate.error = "Required when sputum collected"
            isValid = false
        } else binding.tilSputumCollectionDate.error = null

        if (binding.switchReferredToHigherCentre.isChecked &&
            binding.etReferralPlaceName.text.isNullOrBlank()
        ) {
            binding.tilReferralPlaceName.error = "Required when referred"
            isValid = false
        } else binding.tilReferralPlaceName.error = null

        if (!isValid) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
        }

        return isValid
    }

    private fun saveTbScreening() {
        val entity = TbScreeningEntity(
            patientId = patientId ?: "",
            name = binding.etName.text.toString(),
            ageYears = binding.etAgeYears.text.toString().toIntOrNull() ?: 0,
            sex = getSelectedSex(),
            mobileNumber = binding.etMobileNumber.text.toString().ifBlank { null },
            addressVillage = binding.etAddressVillage.text.toString(),
            ashaIdOrName = binding.etAshaIdOrName.text.toString(),
            dateOfScreening = binding.etDateOfScreening.text.toString(),

            cough2WeeksOrMore = binding.switchCough2Weeks.isChecked,
            coughWithBlood = binding.switchCoughWithBlood.isChecked,
            fever2WeeksOrMore = binding.switchFever2Weeks.isChecked,
            nightSweats = binding.switchNightSweats.isChecked,
            weightLossPoorAppetite = binding.switchWeightLoss.isChecked,
            chestPainOrDifficultyBreathing = binding.switchChestPain.isChecked,
            householdMemberOnTbTreatment = binding.switchHouseholdTb.isChecked,

            previousTbTreatment = binding.switchPreviousTb.isChecked,
            closeContactTbPatient = binding.switchCloseContact.isChecked,
            knownHivPositive = binding.switchHivPositive.isChecked,
            diabetes = binding.switchDiabetes.isChecked,
            smokingOrTobaccoUse = binding.switchSmoking.isChecked,
            alcoholDependence = binding.switchAlcohol.isChecked,

            sputumCollected = binding.switchSputumCollected.isChecked,
            sputumCollectionDate =
                if (binding.switchSputumCollected.isChecked)
                    binding.etSputumCollectionDate.text.toString()
                else null,
            chestXrayAdvised = binding.switchChestXray.isChecked,
            referredToHigherCentre = binding.switchReferredToHigherCentre.isChecked,
            referralPlaceName =
                if (binding.switchReferredToHigherCentre.isChecked)
                    binding.etReferralPlaceName.text.toString()
                else null
        )

        Log.d("TB_SCREENING_UI", "Saving entity = $entity")

        // Save locally only. Sync happens later from dashboard.
        viewModel.saveTbScreening(entity)

        // Optionally close screen immediately
        finish()
    }

    private fun getSelectedSex(): String {
        return when (binding.rgSex.checkedRadioButtonId) {
            R.id.rbSexM -> "Male"
            R.id.rbSexF -> "Female"
            R.id.rbSexO -> "O"
            else -> "M"
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
