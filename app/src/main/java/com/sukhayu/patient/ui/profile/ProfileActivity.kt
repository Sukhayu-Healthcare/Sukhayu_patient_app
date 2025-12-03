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
import com.sukhayu.patient.ui.login.LoginActivity
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
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false


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

    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super. onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure numeric input types for age and phone
        binding.etAge.inputType = InputType.TYPE_CLASS_NUMBER
        binding.etPhone.inputType = InputType.TYPE_CLASS_PHONE

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

        // ensure initial state is read-only
        setFieldsEnabled(false)

        // Image change button click
        binding.btnChangeImage.setOnClickListener {
            showImagePickerDialog()
        }

        // Password visibility toggles
        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.btnToggleConfirmPassword.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }

        // Edit / Save behaviour with basic validation
        binding.btnEdit.setOnClickListener {
            if (!isEditMode) {
                // Enable edit mode
                isEditMode = true
                enableEditMode()
            } else {
                // Save changes
                if (validateAndSave()) {
                    disableEditMode()
                    isEditMode = false
                }
            }
        }

        // Logout -> LoginActivity
        binding.btnLogout.setOnClickListener {
            getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility)
        } else {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        }
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        if (isConfirmPasswordVisible) {
            binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility)
        } else {
            binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off)
        }
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
    }

    private fun enableEditMode() {
        binding.etName.isEnabled = true
        binding.etAge.isEnabled = true
        binding.spinnerGender.isEnabled = true
        binding.etPhone.isEnabled = true

        // Show password fields
        binding.layoutPassword.visibility = android.view.View.VISIBLE
        binding.layoutConfirmPassword.visibility = android.view.View.VISIBLE

        binding.btnEdit.text = "Save"
        // binding.btnEdit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3B82F6"))
    }

    private fun disableEditMode() {
        binding.etName.isEnabled = false
        binding.etAge.isEnabled = false
        binding.spinnerGender.isEnabled = false
        binding.etPhone.isEnabled = false

        // Hide password fields and clear them
        binding.layoutPassword.visibility = android.view.View.GONE
        binding.layoutConfirmPassword.visibility = android.view.View.GONE
        binding.etPassword.text.clear()
        binding.etConfirmPassword.text.clear()

        binding.btnEdit.text = "Edit"
        // binding.btnEdit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#22C55E"))
    }

    private fun isStrongPassword(password: String): Boolean {
        // At least 8 characters
        if (password.length < 8) return false

        // Check for uppercase letter
        if (!password.any { it.isUpperCase() }) return false

        // Check for lowercase letter
        if (!password.any { it.isLowerCase() }) return false

        // Check for digit
        if (!password.any { it.isDigit() }) return false

        // Check for special character
        val specialChars = "!@#\$%^&*()_+-=[]{}|;:',.<>?/"
        if (!password.any { it in specialChars }) return false

        return true
    }

    private fun validateAndSave(): Boolean {
        val name = binding.etName.text.toString().trim()
        val age = binding.etAge.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Basic validations
        if (name.isEmpty() || age.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return false
        }

        // If password is provided, validate it
        if (password.isNotEmpty()) {
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return false
            }

            if (!isStrongPassword(password)) {
                Toast.makeText(
                    this,
                    "Password must be at least 8 characters with uppercase, lowercase, number and special character",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }

        // Proceed with save (add your API call here)
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        return true
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        binding.etName.isEnabled = enabled
        binding.etAge.isEnabled = enabled
        binding.spinnerGender.isEnabled = enabled
        binding.etPhone.isEnabled = enabled
        // patient id remains read-only always
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
    }
}