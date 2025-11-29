package com.sukhayu.patient.ui.supervisor.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.login.LoginActivity

class AshaProfileActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ASHA_ID = "ashaId"
        const val EXTRA_ASHA_NAME = "ashaName"
        const val EXTRA_USER_ID = "userId"
        const val EXTRA_PHONE = "phone"
        const val EXTRA_VILLAGE = "village"
        const val EXTRA_DISTRICT = "district"
        const val EXTRA_TALUKA = "taluka"
        const val EXTRA_ROLE = "role"
    }

    private lateinit var profileImage: ImageView
    private lateinit var btnChangeImage: ImageButton
    private lateinit var etFullName: EditText
    private lateinit var etAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etPhoneNo: EditText
    private lateinit var etAshaId: EditText
    private lateinit var etVillage: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etTaluka: EditText
    private lateinit var etAadharNumber: EditText
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            profileImage.setImageURI(it)
            Toast.makeText(this, "Profile picture updated (not saved to DB)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_profile)

        initViews()
        loadSampleData()
        setFieldsEnabled(false)
        setupListeners()
    }

    private fun initViews() {
        profileImage = findViewById(R.id.profile_image)
        btnChangeImage = findViewById(R.id.btn_change_image)
        etFullName = findViewById(R.id.et_full_name)
        etAge = findViewById(R.id.et_age)
        spinnerGender = findViewById(R.id.spinner_gender)
        etPhoneNo = findViewById(R.id.et_phone_no)
        etAshaId = findViewById(R.id.et_asha_id)
        etVillage = findViewById(R.id.et_village)
        etDistrict = findViewById(R.id.et_district)
        etTaluka = findViewById(R.id.et_taluka)
        etAadharNumber = findViewById(R.id.et_aadhar_number)
        btnEdit = findViewById(R.id.btn_edit)
        btnLogout = findViewById(R.id.btn_logout)

        etAge.inputType = InputType.TYPE_CLASS_NUMBER
        etPhoneNo.inputType = InputType.TYPE_CLASS_NUMBER
        etAadharNumber.inputType = InputType.TYPE_CLASS_NUMBER

        val genders = listOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter
    }

    private fun loadSampleData() {
        val ashaId = intent.getStringExtra(EXTRA_ASHA_ID) ?: "ASHA001"
        val ashaName = intent.getStringExtra(EXTRA_ASHA_NAME) ?: "Priya Sharma"
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: "USR001"
        val phone = intent.getStringExtra(EXTRA_PHONE) ?: "9876543210"
        val village = intent.getStringExtra(EXTRA_VILLAGE) ?: "Nashik Village"
        val district = intent.getStringExtra(EXTRA_DISTRICT) ?: "Nashik"
        val taluka = intent.getStringExtra(EXTRA_TALUKA) ?: "Nashik Taluka"

        etAshaId.setText(ashaId)
        etFullName.setText(ashaName)
        etPhoneNo.setText(phone)
        etVillage.setText(village)
        etDistrict.setText(district)
        etTaluka.setText(taluka)
        etAge.setText("28")
        spinnerGender.setSelection(0)
        etAadharNumber.setText("123456789012")
        profileImage.setImageResource(R.drawable.sample_patient)
    }

    private fun setupListeners() {
        btnChangeImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnEdit.setOnClickListener {
            val enteringEditMode = !etFullName.isEnabled

            if (enteringEditMode) {
                setFieldsEnabled(true)
                btnEdit.text = "Save"
                etFullName.requestFocus()
            } else {
                if (validateInputs()) {
                    // TODO: persist changes to DB or API
                    setFieldsEnabled(false)
                    btnEdit.text = "Edit"
                    Toast.makeText(this, "Profile saved (not persisted yet)", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnLogout.setOnClickListener {
            getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val age = etAge.text.toString().trim()
        val phoneNo = etPhoneNo.text.toString().trim()
        val village = etVillage.text.toString().trim()
        val district = etDistrict.text.toString().trim()
        val taluka = etTaluka.text.toString().trim()
        val aadharNumber = etAadharNumber.text.toString().trim()

        return when {
            fullName.isEmpty() -> {
                Toast.makeText(this, "Please enter full name.", Toast.LENGTH_SHORT).show()
                false
            }
            fullName.length < 3 -> {
                Toast.makeText(this, "Name must be at least 3 characters.", Toast.LENGTH_SHORT).show()
                false
            }
            age.isEmpty() -> {
                Toast.makeText(this, "Please enter age.", Toast.LENGTH_SHORT).show()
                false
            }
            age.toIntOrNull() == null || age.toInt() < 18 -> {
                Toast.makeText(this, "Please enter a valid age (18+).", Toast.LENGTH_SHORT).show()
                false
            }
            phoneNo.isEmpty() -> {
                Toast.makeText(this, "Please enter phone number.", Toast.LENGTH_SHORT).show()
                false
            }
            phoneNo.length != 10 || !phoneNo.all { it.isDigit() } -> {
                Toast.makeText(this, "Please enter a valid 10-digit phone number.", Toast.LENGTH_SHORT).show()
                false
            }
            village.isEmpty() -> {
                Toast.makeText(this, "Please enter village.", Toast.LENGTH_SHORT).show()
                false
            }
            district.isEmpty() -> {
                Toast.makeText(this, "Please enter district.", Toast.LENGTH_SHORT).show()
                false
            }
            taluka.isEmpty() -> {
                Toast.makeText(this, "Please enter taluka.", Toast.LENGTH_SHORT).show()
                false
            }
            aadharNumber.isEmpty() -> {
                Toast.makeText(this, "Please enter Aadhar number.", Toast.LENGTH_SHORT).show()
                false
            }
            aadharNumber.length != 12 || !aadharNumber.all { it.isDigit() } -> {
                Toast.makeText(this, "Please enter a valid 12-digit Aadhar number.", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        etFullName.isEnabled = enabled
        etAge.isEnabled = enabled
        spinnerGender.isEnabled = enabled
        etPhoneNo.isEnabled = enabled
        etVillage.isEnabled = enabled
        etDistrict.isEnabled = enabled
        etTaluka.isEnabled = enabled
        etAadharNumber.isEnabled = enabled
        etAshaId.isEnabled = false
    }
}
