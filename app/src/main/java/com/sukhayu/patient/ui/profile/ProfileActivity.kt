package com.sukhayu.patient.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.sukhayu.patient.R
import com.sukhayu.patient.databinding.ActivityProfileBinding
import com.sukhayu. patient.ui.login.LoginActivity
import com.sukhayu.utils.VoiceInputHelper
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PATIENT_ID = "patientId"
        const val EXTRA_PATIENT_NAME = "patientName"
    }

    private lateinit var binding: ActivityProfileBinding
    private var currentPhotoUri: Uri? = null

    private lateinit var voiceHelper: VoiceInputHelper

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            binding.profileImage.setImageURI(it)
            // Save URI to preferences or database
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            binding.profileImage.setImageURI(currentPhotoUri)
            // Save URI to preferences or database
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super. onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // find views
        // profileImage = findViewById(R.id.profile_image)
        // btnChangeImage = findViewById(R.id.btn_change_image)
        // etName = findViewById(R.id.et_name)
        // etAge = findViewById(R.id.et_age)
        // spinnerGender = findViewById(R.id.spinner_gender)
        // etPhone = findViewById(R.id.et_phone)
        // etPatientId = findViewById(R.id.et_patient_id)
        // btnEdit = findViewById(R.id.btn_edit)
        // btnLogout = findViewById(R.id.btn_logout)

        // Ensure numeric input types for age and phone (keeps IDs same)
        binding.etAge.inputType = InputType.TYPE_CLASS_NUMBER
        binding.etPhone.inputType = InputType.TYPE_CLASS_NUMBER

        // simple gender options
        val genders = listOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = genderAdapter

        // read extras with safe fallbacks
        val patientId = intent.getStringExtra(EXTRA_PATIENT_ID) ?: "P001"
        val patientName = intent.getStringExtra(EXTRA_PATIENT_NAME) ?: "Dummy Patient"

        // populate UI
        binding.etPatientId.setText(patientId)
        binding.etName.setText(patientName)
        binding.etAge.setText("45") // placeholder, replace with real data load later
        binding.etPhone.setText("9876543210")

        // ensure initial state is read-only (kept same as before)
        setFieldsEnabled(false)

        // Image change button click
        binding.btnChangeImage.setOnClickListener {
            showImagePickerDialog()
        }

        // Edit / Save behaviour with basic validation
        binding.btnEdit.setOnClickListener {
            val enteringEditMode = ! binding.etName.isEnabled

            if (enteringEditMode) {
                // enable editing
                setFieldsEnabled(true)
                binding.btnEdit.text = "Save"
                // put focus on name
                binding.etName.requestFocus()
            } else {
                // Attempt to save: validate
                val nameText = binding.etName.text.toString().trim()
                val ageText = binding.etAge.text.toString().trim()
                val phoneText = binding.etPhone.text.toString().trim()

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
                binding.btnEdit.text = "Edit"
                Toast.makeText(this, "Profile saved (not persisted yet)", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout -> LoginActivity
        binding.btnLogout.setOnClickListener {
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
        binding.etName.isEnabled = enabled
        binding.etAge.isEnabled = enabled
        binding.spinnerGender.isEnabled = enabled
        binding.etPhone. isEnabled = enabled
        // patient id remains read-only
        binding.etPatientId.isEnabled = false
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Update Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            currentPhotoUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(currentPhotoUri)
        } catch (e: IOException) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile("PROFILE_${timeStamp}_", ".jpg", storageDir)
    }

    private fun toggleEditMode(enabled: Boolean) {
        binding.etName.isEnabled = enabled
        binding.etAge.isEnabled = enabled
        binding.spinnerGender.isEnabled = enabled
        binding.etPhone.isEnabled = enabled

        if (enabled) {
            binding.btnEdit.text = "Save"
            binding.btnChangeImage.isEnabled = true
        } else {
            binding.btnEdit.text = "Edit"
            binding.btnChangeImage.isEnabled = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}