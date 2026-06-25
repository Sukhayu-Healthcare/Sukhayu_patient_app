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
import com.sukhayu.patient.data.remote.UpdatePatientProfileRequest
import com.sukhayu.patient.data.remote.UpdatePatientProfileResponse
import com.sukhayu.patient.databinding.ActivityProfileBinding
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.utils.HeaderUtils
import com.sukhayu.patient.utils.LocalizableActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
import android.widget.AdapterView
import com.sukhayu.patient.utils.TtsHelper
import com.sukhayu.patient.utils.ViewTtsHelper
class ProfileActivity : LocalizableActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private var isEditMode = false
    private val TAG = "ProfileActivity"
    private var currentDob: String? = null

    private lateinit var ttsHelper: TtsHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupLanguageToggle()
        HeaderUtils.setupRoleInHeader(this)
        setupUI()
        fetchPatientProfile()

        // Initialize TTS
        ttsHelper = TtsHelper(this)

        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val currentLang = prefs.getString("My_Lang", "en") ?: "en"

        ttsHelper.setLanguage(currentLang)

        // Enable TTS on all TextViews and Buttons
        ViewTtsHelper.attachToAllTextViews(
            findViewById(android.R.id.content),
            ttsHelper
        )
    }

    private fun setupUI() {
        // Initial setup
        binding.profileImage.setImageResource(R.drawable.ic_person_24)
        binding.etAge.inputType = InputType.TYPE_CLASS_NUMBER
        binding.etPhone.inputType = InputType.TYPE_CLASS_PHONE

        val genders = listOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = genderAdapter

        // Lock all fields by default
        setFieldsEnabled(false)

        binding.btnTogglePassword.setOnClickListener { togglePasswordVisibility() }
        binding.btnToggleConfirmPassword.setOnClickListener { toggleConfirmPasswordVisibility() }
        binding.btnEdit.setOnClickListener { handleEditSave() }
        binding.btnLogout.setOnClickListener { logout() }
        
        // Populate with cached data immediately so user doesn't see "Default Data" while loading
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        binding.etName.setText(prefs.getString("user_name", ""))
        binding.etPhone.setText(prefs.getString("user_phone", ""))
    }

    private fun fetchPatientProfile() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        var token = prefs.getString("token", "") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            logout()
            return
        }

        // Clean token if it already contains Bearer
        if (token.startsWith("Bearer ")) {
            token = token.substring(7)
        }

        Log.d(TAG, "📡 Fetching Profile... Token prefix: Bearer ${token.take(10)}...")

        ApiClient.retrofit.getPatientProfile("Bearer $token")
            .enqueue(object : Callback<PatientProfileResponse> {
                override fun onResponse(
                    call: Call<PatientProfileResponse>,
                    response: Response<PatientProfileResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        populateProfileUI(response.body()!!)
                    } else {
                        Log.e(TAG, "❌ Failed: ${response.code()} ${response.errorBody()?.string()}")
                        Toast.makeText(this@ProfileActivity, "Failed to load latest profile data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PatientProfileResponse>, t: Throwable) {
                    Log.e(TAG, "❌ Network error: ${t.message}")
                    Toast.makeText(this@ProfileActivity, "Network error. Showing cached data.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun populateProfileUI(response: PatientProfileResponse) {
        val patient = response.patient
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)

        // The /profile backend DOES NOT return user_name. Fallback to cached login name.
        val displayName = when {
            !patient.user_name.isNullOrEmpty() -> patient.user_name
            !patient.name.isNullOrEmpty() -> patient.name
            else -> prefs.getString("user_name", "User")
        }

        binding.etName.setText(displayName)
        binding.etPatientId.setText(patient.patient_id.toString())
        binding.etPhone.setText(patient.phone ?: prefs.getString("user_phone", ""))
        currentDob = patient.dob

        // Handle Gender Selection Case-Insensitively
        patient.gender?.let { gender ->
            val adapter = binding.spinnerGender.adapter as ArrayAdapter<String>
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).equals(gender, ignoreCase = true)) {
                    binding.spinnerGender.setSelection(i)
                    break
                }
            }
        }

        // Handle Age from DOB
        if (!patient.dob.isNullOrEmpty()) {
            val age = calculateAgeFromDOB(patient.dob)
            if (age > 0) binding.etAge.setText(age.toString())
        }
    }

    private fun calculateAgeFromDOB(dob: String): Int {
        val formats = listOf("yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "dd/MM/yyyy")
        for (format in formats) {
            try {
                val formatter = SimpleDateFormat(format, Locale.getDefault())
                val birthDate = formatter.parse(dob) ?: continue
                val today = Calendar.getInstance()
                val birthCalendar = Calendar.getInstance().apply { time = birthDate }

                var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                return age
            } catch (e: Exception) { /* Try next format */ }
        }
        return 0
    }

    private fun handleEditSave() {
        if (!isEditMode) {
            isEditMode = true
            enableEditMode()
        } else {
            validateAndSave()
        }
    }

    private fun enableEditMode() {
        // Only allow password editing. Personal info fields remain disabled.
        binding.layoutPassword.visibility = View.VISIBLE
        binding.layoutConfirmPassword.visibility = View.VISIBLE
        binding.btnEdit.text = "Save"
    }

    private fun disableEditMode() {
        binding.layoutPassword.visibility = View.GONE
        binding.layoutConfirmPassword.visibility = View.GONE
        binding.etPassword.text.clear()
        binding.etConfirmPassword.text.clear()
        binding.btnEdit.text = "Edit"
        isEditMode = false
    }

    private fun validateAndSave() {
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (password.isEmpty()) {
            binding.etPassword.error = "Please enter a new password"
            return
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return
        }

        // Send ONLY the password update request
        val request = UpdatePatientProfileRequest(
            password = password
        )

        updateProfile(request)
    }

    private fun updateProfile(request: UpdatePatientProfileRequest) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        var token = prefs.getString("token", "") ?: ""
        if (token.startsWith("Bearer ")) {
            token = token.substring(7)
        }

        binding.btnEdit.isEnabled = false
        binding.btnEdit.text = "Saving..."

        ApiClient.retrofit.updatePatientProfile("Bearer $token", request)
            .enqueue(object : Callback<UpdatePatientProfileResponse> {
                override fun onResponse(
                    call: Call<UpdatePatientProfileResponse>,
                    response: Response<UpdatePatientProfileResponse>
                ) {
                    binding.btnEdit.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        disableEditMode()
                        // Password updated, no need to refresh the whole profile usually, 
                        // but can call fetchPatientProfile() if needed.
                    } else {
                        Log.e(TAG, "❌ Update failed! Code: ${response.code()} | Error: ${response.errorBody()?.string()}")
                        Toast.makeText(this@ProfileActivity, "Failed to update password", Toast.LENGTH_SHORT).show()
                        binding.btnEdit.text = "Save"
                    }
                }

                override fun onFailure(call: Call<UpdatePatientProfileResponse>, t: Throwable) {
                    binding.btnEdit.isEnabled = true
                    binding.btnEdit.text = "Save"
                    Log.e(TAG, "❌ Network error during update: ${t.message}")
                    Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        // Based on the new requirement, personal info fields are always disabled for the user
        binding.etAge.isEnabled = false
        binding.spinnerGender.isEnabled = false
        binding.etPhone.isEnabled = false
        binding.etPatientId.isEnabled = false
        binding.etName.isEnabled = false
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        binding.etPassword.inputType = if (isPasswordVisible)
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.btnTogglePassword.setImageResource(if (isPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off)
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        binding.etConfirmPassword.inputType = if (isConfirmPasswordVisible)
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.btnToggleConfirmPassword.setImageResource(if (isConfirmPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off)
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
    }

    private fun logout() {
        getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
