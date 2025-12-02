package com.sukhayu.patient.ui.asha.registration

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.ApiService
import com.sukhayu.patient.data.remote.PatientDto
import com.sukhayu.patient.data.remote.toEntity
import com.sukhayu.patient.model.HealthHistoryItem
import com.sukhayu.patient.model.PatientRegistrationRequest
import com.sukhayu.patient.model.PatientRegistrationResponse
import com.sukhayu.patient.utils.NetworkUtils
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.utils.VoiceInputHelper
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterPatientActivity : AppCompatActivity() {

    // UI Views
    private lateinit var etPatientName: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var spinnerGender: Spinner
    private lateinit var etDob: EditText
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var btnAddPhoto: Button
    private lateinit var etPhone: EditText
    private lateinit var etVillage: EditText
    private lateinit var etTaluka: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etSupremeId: EditText
    private lateinit var layoutHistoryContainer: LinearLayout
    private lateinit var btnAddDisease: Button
    private lateinit var btnRegisterPatient: Button

    // Data
    private val historyList = mutableListOf<HealthHistoryItem>()
    private var profilePicBase64: String? = null
    private var selectedImageUri: Uri? = null
    private var isPasswordVisible = false
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val isoDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // API Service
    private val apiService: ApiService by lazy { ApiClient.retrofit }

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivProfilePhoto.setImageURI(it)
            // Convert image to Base64
            convertImageToBase64(it)
            Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher for handling permission requests (Android 6.0+)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.all { it.value }
        if (allPermissionsGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(
                this,
                "Permission denied. Cannot access gallery.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_register_patient)

        // Initialize TokenManager
        TokenManager.init(this)

        // Find views
        findViews()

        // Setup UI
        setupGenderSpinner()
        setupDatePicker()
        setupPasswordToggle()
        setupImagePicker()
        setupDynamicHistoryContainer()
        setupRegisterButton()

        // Request audio permission
        requestAudioPermission()

        // Initialize voice helper and attach to all EditTexts
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }


    private fun findViews() {
        etPatientName = findViewById(R.id.et_patient_name)
        etPassword = findViewById(R.id.et_password)
        btnTogglePassword = findViewById(R.id.btn_toggle_password)
        spinnerGender = findViewById(R.id.spinner_gender)
        etDob = findViewById(R.id.et_dob)
        ivProfilePhoto = findViewById(R.id.iv_profile_photo)
        btnAddPhoto = findViewById(R.id.btn_add_photo)
        etPhone = findViewById(R.id.et_phone)
        etVillage = findViewById(R.id.et_village)
        etTaluka = findViewById(R.id.et_taluka)
        etDistrict = findViewById(R.id.et_district)
        etSupremeId = findViewById(R.id.et_supreme_id)
        layoutHistoryContainer = findViewById(R.id.layout_history_container)
        btnAddDisease = findViewById(R.id.btn_add_disease)
        btnRegisterPatient = findViewById(R.id.btn_register_patient)
    }

    private fun setupGenderSpinner() {
        val genders = listOf("Select Gender", "Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = adapter
    }

    private fun setupDatePicker() {
        etDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val selectedDate = calendar.time
                    etDob.setText(dateFormatter.format(selectedDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupPasswordToggle() {
        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                etPassword.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
            }
            etPassword.setSelection(etPassword.text.length)
        }
    }

    private fun setupImagePicker() {
        btnAddPhoto.setOnClickListener {
            requestImagePickerPermissions()
        }
    }

    private fun requestImagePickerPermissions() {
        // For Android 6.0+ (API 23+), request runtime permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = mutableListOf<String>()

            // Check READ_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            // Check CAMERA permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.CAMERA)
            }

            // If permissions are not granted, request them
            if (permissions.isNotEmpty()) {
                permissionLauncher.launch(permissions.toTypedArray())
            } else {
                // All permissions already granted
                imagePickerLauncher.launch("image/*")
            }
        } else {
            // For devices below Android 6.0, permissions are granted at install time
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun setupDynamicHistoryContainer() {
        btnAddDisease.setOnClickListener {
            addDiseaseRow()
        }
    }

    private fun addDiseaseRow() {
        val rowContainer = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.rounded_edittext)
            setPadding(8, 0, 8, 0)
        }

        val etDiseaseName = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.5f
            )
            hint = "Disease name"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setPadding(12, 12, 12, 12)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            textSize = 14f
        }

        val etDuration = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.5f
            )
            hint = "Duration (e.g. 2 years)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setPadding(12, 12, 12, 12)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            textSize = 14f
        }

        val btnRemove = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(48, 48)
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            contentDescription = "Remove disease"
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(8, 8, 8, 8)
            setOnClickListener {
                layoutHistoryContainer.removeView(rowContainer)
                // Remove from list
                historyList.removeIf { it.disease == etDiseaseName.text.toString() && it.duration == etDuration.text.toString() }
            }
        }

        rowContainer.addView(etDiseaseName)
        rowContainer.addView(etDuration)
        rowContainer.addView(btnRemove)
        layoutHistoryContainer.addView(rowContainer)
    }

    private fun setupRegisterButton() {
        btnRegisterPatient.setOnClickListener {
            if (validateForm()) {
                collectHealthHistory()
                submitRegistration()
            }
        }
    }

    private fun validateForm(): Boolean {
        // Validate Patient Name
        if (etPatientName.text.isBlank()) {
            Toast.makeText(this, "Please enter patient name.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate Password
        if (etPassword.text.isBlank()) {
            Toast.makeText(this, "Please enter password.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate Gender
        if (spinnerGender.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select gender.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate DOB
        if (etDob.text.isBlank()) {
            Toast.makeText(this, "Please enter date of birth.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate Phone (10 digits)
        val phone = etPhone.text.toString().trim()
        if (phone.isBlank()) {
            Toast.makeText(this, "Please enter phone number.", Toast.LENGTH_SHORT).show()
            return false
        }
        // Remove +91 prefix if present and validate 10 digits
        val phoneDigitsOnly = phone.replace(Regex("[^0-9]"), "")
        if (phoneDigitsOnly.length != 10) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate Village
        if (etVillage.text.isBlank()) {
            Toast.makeText(this, "Please enter village.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate Taluka
        if (etTaluka.text.isBlank()) {
            Toast.makeText(this, "Please enter taluka.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate District
        if (etDistrict.text.isBlank()) {
            Toast.makeText(this, "Please enter district.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun collectHealthHistory() {
        historyList.clear()
        for (i in 0 until layoutHistoryContainer.childCount) {
            val rowContainer = layoutHistoryContainer.getChildAt(i) as? LinearLayout ?: continue
            val etDiseaseName = rowContainer.getChildAt(0) as? EditText ?: continue
            val etDuration = rowContainer.getChildAt(1) as? EditText ?: continue

            val disease = etDiseaseName.text.toString().trim()
            val duration = etDuration.text.toString().trim()

            if (disease.isNotBlank() && duration.isNotBlank()) {
                historyList.add(HealthHistoryItem(disease, duration))
            }
        }
    }

    private fun submitRegistration() {
        // Check network
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(
                this,
                "No internet connection. Please check your network.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Disable button to prevent double-submission
        btnRegisterPatient.isEnabled = false
        btnRegisterPatient.text = "Registering..."

        // Get token from TokenManager
        val token = TokenManager.getToken()
        if (token.isEmpty()) {
            Toast.makeText(
                this,
                "Session expired. Please login again.",
                Toast.LENGTH_LONG
            ).show()
            btnRegisterPatient.isEnabled = true
            btnRegisterPatient.text = "Register Patient"
            return
        }

        // Prepare date in ISO format (yyyy-MM-dd)
        val dobISO = convertDobToISO(etDob.text.toString())
        if (dobISO.isEmpty()) {
            Toast.makeText(this, "Invalid date format.", Toast.LENGTH_SHORT).show()
            btnRegisterPatient.isEnabled = true
            btnRegisterPatient.text = "Register Patient"
            return
        }

        // Clean phone number (remove non-digits)
        val phoneDigitsOnly = etPhone.text.toString().replace(Regex("[^0-9]"), "")

        // Parse supreme_id (null if empty, otherwise convert to Int)
        val supremeId = if (etSupremeId.text.isBlank()) {
            null
        } else {
            etSupremeId.text.toString().toIntOrNull()
        }

        // Create request object
        val request = PatientRegistrationRequest(
            name = etPatientName.text.toString().trim(),
            password = etPassword.text.toString(),
            gender = spinnerGender.selectedItem.toString(),
            dob = dobISO,
            phone = phoneDigitsOnly,
            profile_pic = profilePicBase64,
            village = etVillage.text.toString().trim(),
            taluka = etTaluka.text.toString().trim(),
            district = etDistrict.text.toString().trim(),
            history = historyList,
            supreme_id = supremeId
        )

        Log.d("RegisterPatient", "Submitting registration for: ${request.name}")

        // Make API call with Bearer token
        apiService.registerPatient("Bearer $token", request)
            .enqueue(object : Callback<PatientRegistrationResponse> {
                override fun onResponse(
                    call: Call<PatientRegistrationResponse>,
                    response: Response<PatientRegistrationResponse>
                ) {
                    // Re-enable button
                    btnRegisterPatient.isEnabled = true
                    btnRegisterPatient.text = "Register Patient"

                    when {
                        response.isSuccessful && response.body() != null -> {
                            val responseBody = response.body()!!
                            Log.d("RegisterPatient", "Success: Patient ID=${responseBody.patient_id}, Supreme ID=${responseBody.supreme_id}")
                            cachePatientLocally(responseBody)
                            showSuccessDialog(responseBody)
                        }
                        response.code() == 401 -> {
                            // Unauthorized - token might be invalid
                            Toast.makeText(
                                this@RegisterPatientActivity,
                                "Session expired. Please login again.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        response.code() == 400 -> {
                            // Bad request - validation error
                            val errorMessage = try {
                                response.errorBody()?.string() ?: "Invalid data provided"
                            } catch (e: Exception) {
                                "Invalid data provided"
                            }
                            Log.e("RegisterPatient", "Validation error: $errorMessage")
                            Toast.makeText(
                                this@RegisterPatientActivity,
                                "Registration failed. Please check all fields.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        response.code() == 500 -> {
                            // Server error
                            Toast.makeText(
                                this@RegisterPatientActivity,
                                "Server error. Please try again later.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            // Other errors
                            val errorMessage = response.message() ?: "Unknown error"
                            Log.e("RegisterPatient", "Error ${response.code()}: $errorMessage")
                            Toast.makeText(
                                this@RegisterPatientActivity,
                                "Registration failed: $errorMessage",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<PatientRegistrationResponse>, t: Throwable) {
                    // Re-enable button
                    btnRegisterPatient.isEnabled = true
                    btnRegisterPatient.text = "Register Patient"

                    // Log error
                    Log.e("RegisterPatient", "Network failure", t)

                    // Show user-friendly error message
                    Toast.makeText(
                        this@RegisterPatientActivity,
                        "Network error. Please check your connection and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun convertDobToISO(displayDate: String): String {
        return try {
            val parsedDate = dateFormatter.parse(displayDate) ?: return ""
            isoDateFormatter.format(parsedDate)
        } catch (e: Exception) {
            Log.e("RegisterPatient", "Date conversion error", e)
            ""
        }
    }

    private fun convertImageToBase64(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.let {
                val bytes = it.readBytes()
                profilePicBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                it.close()
                Log.d("RegisterPatient", "Image converted to Base64: ${profilePicBase64?.length} bytes")
            }
        } catch (e: Exception) {
            Log.e("RegisterPatient", "Image conversion error", e)
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccessDialog(response: PatientRegistrationResponse) {
        val patientId = response.patient_id ?: "N/A"
        val supremeId = response.supreme_id ?: "N/A"

        val message =
            "Patient registered successfully!\n\nPatient ID: $patientId\nSupreme ID: $supremeId"

        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("Register Another Patient") { _, _ ->
                resetForm()
            }
            .setNegativeButton("Go Back") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun resetForm() {
        etPatientName.text.clear()
        etPassword.text.clear()
        spinnerGender.setSelection(0)
        etDob.text.clear()
        etPhone.text.clear()
        etVillage.text.clear()
        etTaluka.text.clear()
        etDistrict.text.clear()
        etSupremeId.text.clear()
        ivProfilePhoto.setImageDrawable(null)
        layoutHistoryContainer.removeAllViews()
        historyList.clear()
        profilePicBase64 = null
        selectedImageUri = null
        isPasswordVisible = false
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    private fun cachePatientLocally(response: PatientRegistrationResponse) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AshaLocalDatabase.getInstance(applicationContext)
                val dto = PatientDto(
                    id = response.patient_id?.toString() ?: return@launch,
                    patient_id = response.patient_id.toString(),
                    name = etPatientName.text.toString().trim(),
                    user_name = etPatientName.text.toString().trim(),
                    phone = etPhone.text.toString().trim(),
                    village = etVillage.text.toString().trim(),
                    district = etDistrict.text.toString().trim(),
                    gender = spinnerGender.selectedItem.toString(),
                    weight_kg = null,
                    supreme_id = response.supreme_id?.toString()
                )
                db.patientDao().insertOrUpdate(dto.toEntity())
            } catch (e: Exception) {
                Log.e("RegisterPatient", "Failed to cache patient", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
