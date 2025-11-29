package com.sukhayu.patient.ui.supervisor.profile

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.SupervisorProfile
import com.sukhayu.patient.data.remote.UpdateProfileResponse
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AshaProfileActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    // Header Views
    private lateinit var tvName: TextView
    private lateinit var tvId: TextView
    private lateinit var profileImage: ImageView
    private lateinit var btnChangeImage: ImageButton

    // View containers
    private lateinit var cardViewContainer: LinearLayout
    private lateinit var formViewContainer: LinearLayout

    // Display TextViews
    private lateinit var tvAshaId: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvPhoneNo: TextView
    private lateinit var tvVillage: TextView
    private lateinit var tvDistrict: TextView
    private lateinit var tvTaluka: TextView

    // Form Fields
    private lateinit var etFullName: EditText
    private lateinit var etAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etPhoneNo: EditText
    private lateinit var etAshaId: EditText
    private lateinit var etVillage: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etTaluka: EditText

    // Buttons
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            profileImage.setImageURI(uri)
        }
    }

    private var selectedDateOfBirth: String? = null  // Store selected DOB in yyyy-MM-dd format

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervisor_profile)

        // Initialize TokenManager
        TokenManager.init(this)

        initViews()
        setFieldsEnabled(false)
        loadData()
        setupListeners()
    }

    private fun initViews() {
        tvName = findViewById(R.id.tvPatientName)
        tvId = findViewById(R.id.tvPatientId)

        profileImage = findViewById(R.id.profile_image)
        btnChangeImage = findViewById(R.id.btn_change_image)

        cardViewContainer = findViewById(R.id.card_view_container)
        formViewContainer = findViewById(R.id.form_view_container)

        tvAshaId = findViewById(R.id.tv_asha_id)
        tvFullName = findViewById(R.id.tv_full_name)
        tvAge = findViewById(R.id.tv_age)
        tvGender = findViewById(R.id.tv_gender)
        tvPhoneNo = findViewById(R.id.tv_phone_no)
        tvVillage = findViewById(R.id.tv_village)
        tvDistrict = findViewById(R.id.tv_district)
        tvTaluka = findViewById(R.id.tv_taluka)

        etFullName = findViewById(R.id.et_full_name)
        etAge = findViewById(R.id.et_age)
        spinnerGender = findViewById(R.id.spinner_gender)
        etPhoneNo = findViewById(R.id.et_phone_no)
        etAshaId = findViewById(R.id.et_asha_id)
        etVillage = findViewById(R.id.et_village)
        etDistrict = findViewById(R.id.et_district)
        etTaluka = findViewById(R.id.et_taluka)

        btnEdit = findViewById(R.id.btn_edit)
        btnLogout = findViewById(R.id.btn_logout)

        etAge.inputType = InputType.TYPE_NULL  // Disable keyboard input
        etAge.isFocusable = false
        etAge.isClickable = true
        etPhoneNo.inputType = InputType.TYPE_CLASS_NUMBER

        val genders = listOf("Male", "Female", "Other")
        spinnerGender.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            genders
        )
        
        // Set default to Female and disable
        spinnerGender.setSelection(1) // Index 1 = "Female"
        spinnerGender.isEnabled = false

        // Set click listener for date picker
        etAge.setOnClickListener {
            if (etAge.isEnabled) {
                showDatePicker()
            }
        }
    }

    // Show date picker dialog with spinner mode for easier year selection
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        
        // If there's a selected date, use it, otherwise default to 30 years ago
        if (selectedDateOfBirth != null) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(selectedDateOfBirth!!)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                Log.e("AshaProfile", "Error parsing selected date", e)
            }
        } else {
            // Default to 30 years ago for easier selection
            calendar.add(Calendar.YEAR, -30)
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Use DatePickerDialog with spinner style
        val datePickerDialog = DatePickerDialog(
            this,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format selected date as yyyy-MM-dd for backend
                selectedDateOfBirth = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                
                // Format for display as "15 May 1990"
                val displayCalendar = Calendar.getInstance()
                displayCalendar.set(selectedYear, selectedMonth, selectedDay)
                val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                etAge.setText(displayFormat.format(displayCalendar.time))
                
                Log.d("AshaProfile", "Selected DOB: $selectedDateOfBirth")
            },
            year,
            month,
            day
        )

        // Set date range: min 100 years ago, max today
        val minDate = Calendar.getInstance()
        minDate.add(Calendar.YEAR, -100)
        datePickerDialog.datePicker.minDate = minDate.timeInMillis
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        
        // Use spinner mode instead of calendar for easier year selection
        try {
            datePickerDialog.datePicker.calendarViewShown = false
            datePickerDialog.datePicker.spinnersShown = true
        } catch (e: Exception) {
            Log.e("AshaProfile", "Could not set spinner mode", e)
        }
        
        datePickerDialog.show()
    }

    // ------------------ LOAD DATA ------------------
    private fun loadData() {
        // Get token from TokenManager (initialized in onCreate)
        val token = TokenManager.getToken()

        Log.d("AshaProfile", "=== API CALL DEBUG ===")
        Log.d("AshaProfile", "Token exists: ${token.isNotEmpty()}")
        Log.d("AshaProfile", "Token value: $token")

        if (token.isEmpty()) {
            toast("Missing authentication token")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Call GET /asha/profile (returns profile of logged-in user)
        val authHeader = "Bearer $token"
        val call = ApiClient.retrofit.getSupervisorProfile(authHeader)

        Log.d("AshaProfile", "Request URL: ${call.request().url}")
        Log.d("AshaProfile", "Authorization header: $authHeader")

        call.enqueue(object : Callback<SupervisorProfile> {
            override fun onResponse(
                call: Call<SupervisorProfile>,
                response: Response<SupervisorProfile>
            ) {
                Log.d("AshaProfile", "=== RESPONSE DEBUG ===")
                Log.d("AshaProfile", "Response code: ${response.code()}")
                Log.d("AshaProfile", "Response message: ${response.message()}")
                Log.d("AshaProfile", "Response body: ${response.body()}")

                val errorBody = response.errorBody()?.string()
                Log.d("AshaProfile", "Error body: $errorBody")

                val body = response.body()
                if (response.isSuccessful && body != null) {
                    displayProfileData(body)
                    toast("Profile loaded successfully")
                } else {
                    toast("Failed: ${response.code()} - $errorBody")
                    if (response.code() == 401) {
                        // Token invalid, redirect to login
                        TokenManager.clearToken()
                        startActivity(Intent(this@AshaProfileActivity, LoginActivity::class.java))
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<SupervisorProfile>, t: Throwable) {
                Log.e("AshaProfile", "=== API FAILURE ===", t)
                toast("Error: ${t.message}")
            }
        })
    }

    // ------------------ DISPLAY IN UI ------------------
    private fun displayProfileData(profile: SupervisorProfile) {
        Log.d("AshaProfile", "=== DISPLAYING PROFILE ===")
        Log.d("AshaProfile", "Full profile object: $profile")
        Log.d("AshaProfile", "Date of Birth value: '${profile.date_of_birth}'")
        Log.d("AshaProfile", "Date of Birth is null: ${profile.date_of_birth == null}")
        Log.d("AshaProfile", "Date of Birth is empty: ${profile.date_of_birth?.isEmpty()}")
        
        tvName.text = profile.user_name
        tvId.text = "Supervisor ID: ${profile.asha_id}"

        // Display in card view
        tvAshaId.text = profile.asha_id
        tvFullName.text = profile.user_name
        tvPhoneNo.text = profile.phone
        tvVillage.text = profile.village
        tvDistrict.text = profile.district
        tvTaluka.text = profile.taluka

        // Show date of birth instead of age
        val dob = if (profile.date_of_birth != null && profile.date_of_birth.isNotEmpty()) {
            selectedDateOfBirth = profile.date_of_birth  // Store original format
            formatDateOfBirth(profile.date_of_birth)
        } else {
            // Fallback: show account creation date
            Log.d("AshaProfile", "No DOB found, using account creation date")
            selectedDateOfBirth = null
            formatDateOfBirth(profile.user_created_at)
        }
        
        tvAge.text = dob  // Display DOB (field is named tvAge but shows DOB)
        tvGender.text = "Female"    // Always show Female
        
        Log.d("AshaProfile", "Formatted DOB: '$dob'")

        // Fill edit form fields
        etAshaId.setText(profile.asha_id)
        etFullName.setText(profile.user_name)
        etPhoneNo.setText(profile.phone)
        etVillage.setText(profile.village)
        etDistrict.setText(profile.district)
        etTaluka.setText(profile.taluka)
        etAge.setText(dob)  // Show DOB in edit field too
        
        // Set gender to Female
        spinnerGender.setSelection(1) // Index 1 = "Female"

        // Handle profile picture
        when {
            profile.profile_pic == null ->
                profileImage.setImageResource(R.drawable.sample_patient)

            profile.profile_pic.startsWith("content://", true) ||
                    profile.profile_pic.startsWith("file://", true) ->
                profileImage.setImageURI(Uri.parse(profile.profile_pic))

            else ->
                profileImage.setImageResource(R.drawable.sample_patient)
        }
    }

    // Format date of birth for display - returns "15 May 1990" format
    private fun formatDateOfBirth(dateString: String?): String {
        if (dateString.isNullOrEmpty()) {
            return "--"
        }

        try {
            // Parse ISO date format: "1990-05-15" or "1990-05-15T00:00:00.000Z"
            val sdf = if (dateString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            }
            
            val cleanDate = if (dateString.contains("T")) {
                dateString.substring(0, 19)
            } else {
                dateString
            }
            
            val birthDate = sdf.parse(cleanDate) ?: return "--"

            // Format DOB for display as "15 May 1990"
            val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return displayFormat.format(birthDate)
        } catch (e: Exception) {
            Log.e("AshaProfile", "Error formatting date: ${e.message}", e)
            return "--"
        }
    }

    // ------------------ BUTTON LISTENERS ------------------
    private fun setupListeners() {
        btnChangeImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnEdit.setOnClickListener {
            if (etFullName.isEnabled) {
                // Save changes - call API
                saveProfileChanges()
            } else {
                // Switch to editable form
                cardViewContainer.visibility = View.GONE
                formViewContainer.visibility = View.VISIBLE
                setFieldsEnabled(true)
                btnEdit.text = "Save"
            }
        }

        btnLogout.setOnClickListener {
            TokenManager.clearToken()
            getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun saveProfileChanges() {
        val token = TokenManager.getToken()
        if (token.isEmpty()) {
            toast("Authentication token missing")
            return
        }

        // Disable button while saving
        btnEdit.isEnabled = false
        btnEdit.text = "Saving..."

        // Prepare update data - password, phone, and profile_pic are allowed
        val updateData = mutableMapOf<String, Any?>()
        
        val newPhone = etPhoneNo.text.toString().trim()

        // Validate phone
        if (newPhone.isEmpty()) {
            btnEdit.isEnabled = true
            btnEdit.text = "Save"
            toast("Phone number is required")
            return
        }

        // Send allowed fields: asha_phone and asha_profile_pic (backend field names)
        updateData["asha_phone"] = newPhone
        
        // Include profile pic if changed
        if (selectedImageUri != null) {
            updateData["asha_profile_pic"] = selectedImageUri.toString()
        }

        Log.d("AshaProfile", "=== SAVE DEBUG ===")
        Log.d("AshaProfile", "Update data: $updateData")
        Log.d("AshaProfile", "Token: $token")

        val authHeader = "Bearer $token"
        val call = ApiClient.retrofit.updateSupervisorProfile(authHeader, updateData)
        
        Log.d("AshaProfile", "Request URL: ${call.request().url}")
        Log.d("AshaProfile", "Request method: ${call.request().method}")
        Log.d("AshaProfile", "Request headers: ${call.request().headers}")
        
        call.enqueue(object : Callback<UpdateProfileResponse> {
            override fun onResponse(
                call: Call<UpdateProfileResponse>,
                response: Response<UpdateProfileResponse>
            ) {
                btnEdit.isEnabled = true
                
                Log.d("AshaProfile", "=== RESPONSE DEBUG ===")
                Log.d("AshaProfile", "Response code: ${response.code()}")
                Log.d("AshaProfile", "Response message: ${response.message()}")
                Log.d("AshaProfile", "Response headers: ${response.headers()}")
                Log.d("AshaProfile", "Response body: ${response.body()}")
                
                val errorBody = response.errorBody()?.string()
                Log.d("AshaProfile", "Error body: $errorBody")
                
                when {
                    response.isSuccessful -> {
                        val responseBody = response.body()
                        Log.d("AshaProfile", "Success response: $responseBody")
                        toast(responseBody?.message ?: "Profile updated successfully")
                        
                        // Clear selected image
                        selectedImageUri = null
                        
                        // Display updated profile from response
                        responseBody?.profile?.let { displayProfileData(it) }
                        
                        // Switch back to view mode
                        cardViewContainer.visibility = View.VISIBLE
                        formViewContainer.visibility = View.GONE
                        setFieldsEnabled(false)
                        btnEdit.text = "Edit"
                    }
                    response.code() == 403 -> {
                        Log.e("AshaProfile", "403 Forbidden: $errorBody")
                        toast("Only phone and profile picture can be updated")
                        btnEdit.text = "Save"
                    }
                    response.code() == 400 -> {
                        Log.e("AshaProfile", "400 Bad Request: $errorBody")
                        toast("Please provide valid data to update")
                        btnEdit.text = "Save"
                    }
                    response.code() == 500 -> {
                        Log.e("AshaProfile", "500 Internal Server Error: $errorBody")
                        toast("Server error. Please try again later.")
                        btnEdit.text = "Save"
                    }
                    else -> {
                        Log.e("AshaProfile", "HTTP ${response.code()}: $errorBody")
                        toast("Failed to update: ${response.code()}")
                        btnEdit.text = "Save"
                    }
                }
            }

            override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                btnEdit.isEnabled = true
                btnEdit.text = "Save"
                Log.e("AshaProfile", "=== API FAILURE ===", t)
                Log.e("AshaProfile", "Error message: ${t.message}")
                Log.e("AshaProfile", "Error cause: ${t.cause}")
                toast("Network error: ${t.message}")
            }
        })
    }

    private fun updateCardViewData() {
        tvFullName.text = etFullName.text
        tvAge.text = etAge.text
        tvGender.text = spinnerGender.selectedItem.toString()
        tvPhoneNo.text = etPhoneNo.text
        tvVillage.text = etVillage.text
        tvDistrict.text = etDistrict.text
        tvTaluka.text = etTaluka.text
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        etFullName.isEnabled = false     // Name cannot be edited by ASHA
        etAge.isEnabled = false          // DOB cannot be edited (not supported by backend)
        etPhoneNo.isEnabled = enabled    // Phone can be edited
        etVillage.isEnabled = false      // Village cannot be edited by ASHA
        etDistrict.isEnabled = false     // District cannot be edited by ASHA
        etTaluka.isEnabled = false       // Taluka cannot be edited by ASHA
        etAshaId.isEnabled = false       // Supervisor ID is never editable
        spinnerGender.isEnabled = false  // Gender is always Female
        
        // Enable/disable profile image change button
        btnChangeImage.isEnabled = enabled
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}