package com.sukhayu.patient.ui.supervisor.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.*
import com.sukhayu.patient.data.repository.SupervisorRepository
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.utils.TokenManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SupervisorAshaProfileActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    private lateinit var tvName: TextView
    private lateinit var tvId: TextView
    private lateinit var profileImage: ImageView
    private lateinit var btnChangeImage: ImageButton

    private lateinit var cardViewContainer: View
    private lateinit var formViewContainer: View

    private lateinit var tvAshaId: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvPhoneNo: TextView
    private lateinit var tvVillage: TextView
    private lateinit var tvDistrict: TextView
    private lateinit var tvTaluka: TextView

    private lateinit var etFullName: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etPhoneNo: EditText
    private lateinit var etAshaId: EditText
    private lateinit var etVillage: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etTaluka: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnToggleConfirmPassword: ImageButton

    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    private lateinit var repository: SupervisorRepository

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            profileImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_supervisor_profile)
            TokenManager.init(this)
            
            repository = SupervisorRepository(this)
            
            initViews()
            setFieldsEnabled(false)
            loadData()
            setupListeners()

            requestAudioPermission()
        } catch (e: Exception) {
            Log.e("AshaProfile", "Error in onCreate", e)
            toast("Error initializing profile: ${e.message}")
            finish()
        }
    }

    private fun initViews() {
        try {
            tvName = findViewById(R.id.tvPatientName)
            tvId = findViewById(R.id.tvPatientId)
            profileImage = findViewById(R.id.profile_image)
            btnChangeImage = findViewById(R.id.btn_change_image)

            cardViewContainer = findViewById(R.id.card_view_container)
            formViewContainer = findViewById(R.id.form_view_container)

            tvAshaId = findViewById(R.id.tv_asha_id)
            tvFullName = findViewById(R.id.tv_full_name)
            tvGender = findViewById(R.id.tv_gender)
            tvPhoneNo = findViewById(R.id.tv_phone_no)
            tvVillage = findViewById(R.id.tv_village)
            tvDistrict = findViewById(R.id.tv_district)
            tvTaluka = findViewById(R.id.tv_taluka)

            etAshaId = findViewById(R.id.et_asha_id)
            etFullName = findViewById(R.id.et_full_name)
            etPhoneNo = findViewById(R.id.et_phone_no)
            spinnerGender = findViewById(R.id.spinner_gender)
            etVillage = findViewById(R.id.et_village)
            etDistrict = findViewById(R.id.et_district)
            etTaluka = findViewById(R.id.et_taluka)
            etPassword = findViewById(R.id.et_password)
            etConfirmPassword = findViewById(R.id.et_confirm_password)
            btnTogglePassword = findViewById(R.id.btn_toggle_password)
            btnToggleConfirmPassword = findViewById(R.id.btn_toggle_confirm_password)

            btnEdit = findViewById(R.id.btn_edit)
            btnLogout = findViewById(R.id.btn_logout)

            etPhoneNo.inputType = InputType.TYPE_CLASS_PHONE

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("Male", "Female", "Other")
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGender.adapter = adapter
            spinnerGender.setSelection(1) // Female
            spinnerGender.isEnabled = false
            
            Log.d("AshaProfile", "All views initialized successfully")
        } catch (e: Exception) {
            Log.e("AshaProfile", "Error initializing views", e)
            throw e
        }
    }

    private fun loadData() {
        val token = TokenManager.getToken()
        
        Log.d("AshaProfile", "Loading profile with token: ${token.take(20)}...")
        
        if (token.isEmpty()) {
            toast("Missing authentication token")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            val result = repository.getSupervisorProfile()
            
            result.onSuccess { profile ->
                Log.d("AshaProfile", "Profile loaded: $profile")
                displayProfileData(profile)
                toast("Profile loaded successfully")
            }.onFailure { error ->
                Log.e("AshaProfile", "Failed to load profile", error)
                toast("Failed to load profile: ${error.message}")
                
                if (error.message?.contains("401") == true) {
                    TokenManager.clearToken()
                    startActivity(Intent(this@SupervisorAshaProfileActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun displayProfileData(profile: SupervisorProfile) {
        try {
            Log.d("AshaProfile", "Displaying profile data")
            
            tvName.text = profile.user_name ?: "N/A"
            tvId.text = "Supervisor ID: ${profile.asha_id ?: "--"}"

            tvAshaId.text = profile.asha_id ?: "N/A"
            tvFullName.text = profile.user_name ?: "N/A"
            tvGender.text = "Female"
            tvPhoneNo.text = profile.phone ?: "N/A"
            tvVillage.text = profile.village ?: "N/A"
            tvDistrict.text = profile.district ?: "N/A"
            tvTaluka.text = profile.taluka ?: "N/A"

            etAshaId.setText(profile.asha_id ?: "")
            etFullName.setText(profile.user_name ?: "")
            etPhoneNo.setText(profile.phone ?: "")
            etVillage.setText(profile.village ?: "")
            etDistrict.setText(profile.district ?: "")
            etTaluka.setText(profile.taluka ?: "")
            etPassword.setText("")
            etConfirmPassword.setText("")

            profileImage.setImageResource(R.drawable.sample_patient)
            
            Log.d("AshaProfile", "Profile data displayed successfully")
        } catch (e: Exception) {
            Log.e("AshaProfile", "Error displaying profile", e)
            toast("Error displaying profile: ${e.message}")
        }
    }

    private fun setupListeners() {
        btnChangeImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        btnToggleConfirmPassword.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }

        btnEdit.setOnClickListener {
            if (etPhoneNo.isEnabled) {
                saveProfileChanges()
            } else {
                cardViewContainer.visibility = View.GONE
                formViewContainer.visibility = View.VISIBLE
                setFieldsEnabled(true)
                btnEdit.text = "Save"
            }
        }

        btnLogout.setOnClickListener {
            TokenManager.clearToken()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility)
        } else {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        }
        etPassword.setSelection(etPassword.text.length)
    }

    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        if (isConfirmPasswordVisible) {
            etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility)
        } else {
            etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off)
        }
        etConfirmPassword.setSelection(etConfirmPassword.text.length)
    }

    private fun saveProfileChanges() {
        val token = TokenManager.getToken()
        if (token.isEmpty()) {
            toast("Token missing")
            return
        }

        val newPhone = etPhoneNo.text.toString().trim()
        val newPassword = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (newPhone.isEmpty()) {
            toast("Phone number is required")
            return
        }

        // Validate password if provided
        if (newPassword.isNotEmpty()) {
            if (!isPasswordStrong(newPassword)) {
                toast("Password must be at least 8 characters with uppercase, lowercase, digit, and special character")
                return
            }
            
            if (newPassword != confirmPassword) {
                toast("Passwords do not match")
                return
            }
        } else if (confirmPassword.isNotEmpty()) {
            toast("Please enter password in both fields")
            return
        }

        btnEdit.isEnabled = false
        btnEdit.text = "Saving..."

        val updateRequest = SelfUpdateRequest(
            asha_phone = newPhone,
            asha_password = if (newPassword.isNotEmpty()) newPassword else null,
            asha_profile_pic = "dummy_profile_pic"
        )

        Log.d("AshaProfile", "Updating profile: $updateRequest")

        lifecycleScope.launch {
            val result = repository.updateSupervisorProfile(updateRequest)
            
            btnEdit.isEnabled = true
            
            result.onSuccess { updatedProfile ->
                toast("Profile updated successfully")
                displayProfileData(updatedProfile)

                formViewContainer.visibility = View.GONE
                cardViewContainer.visibility = View.VISIBLE
                setFieldsEnabled(false)
                btnEdit.text = "Edit"
            }.onFailure { error ->
                Log.e("AshaProfile", "Update failed", error)
                toast("Update failed: ${error.message}")
                btnEdit.text = "Save"
            }
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        if (password.length < 8) return false
        
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        etPhoneNo.isEnabled = enabled
        etPassword.isEnabled = enabled
        etConfirmPassword.isEnabled = enabled
        btnChangeImage.isEnabled = enabled

        etFullName.isEnabled = false
        etVillage.isEnabled = false
        etDistrict.isEnabled = false
        etTaluka.isEnabled = false
        etAshaId.isEnabled = false
        spinnerGender.isEnabled = false
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
