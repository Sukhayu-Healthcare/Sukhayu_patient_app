package com.sukhayu.patient.ui.profile

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.databinding.ActivityProfileBinding
import com.sukhayu.patient.ui.login.LoginActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dummy profile image
        binding.profileImage.setImageResource(R.drawable.ic_person_24)

        // Ensure numeric input types for age and phone
        binding.etAge.inputType = InputType.TYPE_CLASS_NUMBER
        binding.etPhone.inputType = InputType.TYPE_CLASS_PHONE

        // Gender spinner
        val genders = listOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = genderAdapter

        // Dummy profile data
        binding.etPatientId.setText("P001")
        binding.etName.setText("John Doe")
        binding.etAge.setText("30")
        binding.etPhone.setText("9876543210")
        binding.spinnerGender.setSelection(0) // Male

        // Initial state read-only
        setFieldsEnabled(false)

        // Button listeners
        binding.btnTogglePassword.setOnClickListener { togglePasswordVisibility() }
        binding.btnToggleConfirmPassword.setOnClickListener { toggleConfirmPasswordVisibility() }
        binding.btnEdit.setOnClickListener { handleEditSave() }
        binding.btnLogout.setOnClickListener { logout() }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        binding.etPassword.inputType =
            if (isPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.btnTogglePassword.setImageResource(
            if (isPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
        )
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        binding.etConfirmPassword.inputType =
            if (isConfirmPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.btnToggleConfirmPassword.setImageResource(
            if (isConfirmPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
        )
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
    }

    private fun handleEditSave() {
        if (!isEditMode) {
            isEditMode = true
            enableEditMode()
        } else {
            if (validateAndSave()) {
                disableEditMode()
                isEditMode = false
            }
        }
    }

    private fun enableEditMode() {
        binding.etName.isEnabled = true
        binding.etAge.isEnabled = true
        binding.spinnerGender.isEnabled = true
        binding.etPhone.isEnabled = true
        binding.layoutPassword.visibility = android.view.View.VISIBLE
        binding.layoutConfirmPassword.visibility = android.view.View.VISIBLE
        binding.btnEdit.text = "Save"
    }

    private fun disableEditMode() {
        binding.etName.isEnabled = false
        binding.etAge.isEnabled = false
        binding.spinnerGender.isEnabled = false
        binding.etPhone.isEnabled = false
        binding.layoutPassword.visibility = android.view.View.GONE
        binding.layoutConfirmPassword.visibility = android.view.View.GONE
        binding.etPassword.text.clear()
        binding.etConfirmPassword.text.clear()
        binding.btnEdit.text = "Edit"
    }

    private fun validateAndSave(): Boolean {
        val name = binding.etName.text.toString().trim()
        val age = binding.etAge.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (name.isEmpty() || age.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return false
        }

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

        Toast.makeText(this, "Profile updated successfully (dummy)", Toast.LENGTH_SHORT).show()
        return true
    }

    private fun isStrongPassword(password: String): Boolean {
        if (password.length < 8) return false
        if (!password.any { it.isUpperCase() }) return false
        if (!password.any { it.isLowerCase() }) return false
        if (!password.any { it.isDigit() }) return false
        val specialChars = "!@#\$%^&*()_+-=[]{}|;:',.<>?/"
        if (!password.any { it in specialChars }) return false
        return true
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        binding.etName.isEnabled = enabled
        binding.etAge.isEnabled = enabled
        binding.spinnerGender.isEnabled = enabled
        binding.etPhone.isEnabled = enabled
        binding.etPatientId.isEnabled = false
    }

    private fun logout() {
        getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
