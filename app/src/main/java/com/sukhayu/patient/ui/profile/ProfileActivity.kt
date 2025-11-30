package com.sukhayu.patient. ui. profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net. Uri
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.R
import com.sukhayu. patient.ui.login.LoginActivity
import com.sukhayu.utils.VoiceInputHelper

class ProfileActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PATIENT_ID = "patientId"
        const val EXTRA_PATIENT_NAME = "patientName"
    }

    private lateinit var profileImage: ImageView
    private lateinit var btnChangeImage: ImageButton
    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etPhone: EditText
    private lateinit var etPatientId: EditText
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            profileImage.setImageURI(it)
            Toast.makeText(this, "Profile picture updated (not saved to DB)", Toast.LENGTH_SHORT).show()
            // TODO: When DB is added, persist the image URI or upload to server
        }
    }

    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super. onCreate(savedInstanceState)
        setContentView(R.layout. activity_profile)

        // find views
        profileImage = findViewById(R.id.profile_image)
        btnChangeImage = findViewById(R.id.btn_change_image)
        etName = findViewById(R.id.et_name)
        etAge = findViewById(R.id.et_age)
        spinnerGender = findViewById(R.id.spinner_gender)
        etPhone = findViewById(R.id.et_phone)
        etPatientId = findViewById(R.id.et_patient_id)
        btnEdit = findViewById(R.id.btn_edit)
        btnLogout = findViewById(R.id.btn_logout)

        // Ensure numeric input types for age and phone (keeps IDs same)
        etAge.inputType = InputType.TYPE_CLASS_NUMBER
        etPhone.inputType = InputType.TYPE_CLASS_NUMBER

        // simple gender options
        val genders = listOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter

        // read extras with safe fallbacks
        val patientId = intent.getStringExtra(EXTRA_PATIENT_ID) ?: "P001"
        val patientName = intent.getStringExtra(EXTRA_PATIENT_NAME) ?: "Dummy Patient"

        // populate UI
        etPatientId.setText(patientId)
        etName.setText(patientName)
        etAge.setText("45") // placeholder, replace with real data load later
        etPhone.setText("9876543210")

        // ensure initial state is read-only (kept same as before)
        setFieldsEnabled(false)

        // Image change button click
        btnChangeImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Edit / Save behaviour with basic validation
        btnEdit.setOnClickListener {
            val enteringEditMode = ! etName.isEnabled

            if (enteringEditMode) {
                // enable editing
                setFieldsEnabled(true)
                btnEdit.text = "Save"
                // put focus on name
                etName.requestFocus()
            } else {
                // Attempt to save: validate
                val nameText = etName.text.toString().trim()
                val ageText = etAge.text.toString().trim()
                val phoneText = etPhone.text.toString().trim()

                if (nameText.isEmpty()) {
                    Toast.makeText(this, "Please enter name.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (nameText.length < 3) {
                    Toast.makeText(this, "Name must be at least 3 characters.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (ageText.isEmpty()) {
                    Toast.makeText(this, "Please enter age.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val age = ageText.toIntOrNull()
                if (age == null || age < 18 || age > 130) {
                    Toast.makeText(this, "Please enter a valid age between 18 and 130.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (phoneText.isEmpty()) {
                    Toast.makeText(this, "Please enter phone number.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (phoneText.length != 10 || !phoneText.all { it.isDigit() }) {
                    Toast.makeText(this, "Please enter a valid 10-digit phone number.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // TODO: persist changes to DB or API using patientId
                setFieldsEnabled(false)
                btnEdit.text = "Edit"
                Toast.makeText(this, "Profile saved (not persisted yet)", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout -> LoginActivity
        btnLogout.setOnClickListener {
            getSharedPreferences("auth", MODE_PRIVATE). edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

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

    private fun setFieldsEnabled(enabled: Boolean) {
        etName.isEnabled = enabled
        etAge.isEnabled = enabled
        spinnerGender.isEnabled = enabled
        etPhone. isEnabled = enabled
        // patient id remains read-only
        etPatientId.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}