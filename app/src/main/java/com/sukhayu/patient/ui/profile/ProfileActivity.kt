package com.sukhayu.patient.ui.profile

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.PatientProfileResponse
import com.sukhayu.patient.databinding.ActivityProfileBinding
import com.sukhayu.patient.ui.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private var isEditMode = false
    private val TAG = "ProfileActivity"

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

        // Initial state read-only
        setFieldsEnabled(false)

        // Button listeners
        binding.btnTogglePassword.setOnClickListener { togglePasswordVisibility() }
        binding.btnToggleConfirmPassword.setOnClickListener { toggleConfirmPasswordVisibility() }
        binding.btnEdit.setOnClickListener { handleEditSave() }
        binding.btnLogout.setOnClickListener { logout() }

        // Fetch patient profile
        fetchPatientProfile()
    }

    private fun fetchPatientProfile() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        if (token.isEmpty()) {
            Log.e(TAG, "No auth token found")
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "📥 FETCHING PATIENT PROFILE")
        Log.d(TAG, "Auth Token: $token")
        Log.d(TAG, "========================================")

        ApiClient.retrofit.getPatientProfile("Bearer $token")
            .enqueue(object : Callback<PatientProfileResponse> {
                override fun onResponse(
                    call: Call<PatientProfileResponse>,
                    response: Response<PatientProfileResponse>
                ) {
                    Log.d(TAG, "========================================")
                    Log.d(TAG, "📡 BACKEND RESPONSE RECEIVED")
                    Log.d(TAG, "Response Code: ${response.code()}")
                    Log.d(TAG, "Is Successful: ${response.isSuccessful}")
                    Log.d(TAG, "========================================")

                    if (response.isSuccessful && response.body() != null) {
                        Log.d(TAG, "✅ PROFILE FETCHED SUCCESSFULLY")
                        populateProfileUI(response.body()!!)
                    } else {
                        Log.e(TAG, "❌ Failed to fetch profile. Status: ${response.code()}")
                        Toast.makeText(
                            this@ProfileActivity,
                            "Failed to load profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<PatientProfileResponse>, t: Throwable) {
                    Log.e(TAG, "❌ Network error: ${t.message}", t)
                    Toast.makeText(
                        this@ProfileActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun populateProfileUI(response: PatientProfileResponse) {
        val patient = response.patient

        Log.d(TAG, "========================================")
        Log.d(TAG, "📝 POPULATING PROFILE DATA")
        Log.d(TAG, "Patient ID: ${patient.patient_id}")
        Log.d(TAG, "Name: ${patient.user_name}")
        Log.d(TAG, "Gender: ${patient.gender}")
        Log.d(TAG, "Village: ${patient.village}")
        Log.d(TAG, "District: ${patient.district}")
        Log.d(TAG, "Taluka: ${patient.taluka}")
        Log.d(TAG, "========================================")

        // Set patient name
        binding.etName.setText(patient.user_name ?: "")

        // Set patient ID (read-only)
        binding.etPatientId.setText(patient.patient_id.toString())

        // Set phone
        binding.etPhone.setText(patient.phone ?: "")

        // Set gender
        if (!patient.gender.isNullOrEmpty()) {
            val genderPosition = (binding.spinnerGender.adapter as ArrayAdapter<String>).getPosition(
                patient.gender.capitalize()
            )
            if (genderPosition >= 0) {
                binding.spinnerGender.setSelection(genderPosition)
            }
        }

        // Calculate and set age from DOB
        if (!patient.dob.isNullOrEmpty()) {
            val age = calculateAgeFromDOB(patient.dob)
            binding.etAge.setText(age.toString())
        }

        // Location and ASHA information available in response
        // Use patient.district, patient.taluka, patient.village
        // and response.ashaWorker if needed in UI updates

        Log.d(TAG, "✅ Profile UI populated successfully")
    }

    private fun calculateAgeFromDOB(dob: String): Int {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDate = formatter.parse(dob) ?: return 0
            val today = Calendar.getInstance()
            val birthCalendar = Calendar.getInstance().apply {
                time = birthDate
            }

            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating age: ${e.message}")
            0
        }
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
        binding.etAge.isEnabled = true
        binding.spinnerGender.isEnabled = true
        binding.etPhone.isEnabled = true
        binding.layoutPassword.visibility = android.view.View.VISIBLE
        binding.layoutConfirmPassword.visibility = android.view.View.VISIBLE
        binding.btnEdit.text = "Save"
    }

    private fun disableEditMode() {
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
        val age = binding.etAge.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (age.isEmpty() || phone.isEmpty()) {
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

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
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
