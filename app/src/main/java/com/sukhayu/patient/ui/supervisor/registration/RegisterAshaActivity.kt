package com.sukhayu.patient.ui.supervisor.registration

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.RegisterAshaRequest
import com.sukhayu.patient.data.remote.RegisterAshaResponse
import com.sukhayu.patient.databinding.ActivitySupervisorRegisterAshaBinding
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.utils.VoiceInputHelper
import com.sukhayu.patient.utils.HeaderUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterAshaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupervisorRegisterAshaBinding
    private val TAG = "RegisterAshaActivity"

    // Voice helper attached only to specific fields
    private lateinit var voiceHelper: VoiceInputHelper

    // Hierarchical data structure
    private val districtTalukaVillageData = mapOf(
        "Nagpur" to mapOf(
            "Nagpur Rural" to listOf("Kalmeshwar", "Mouda", "Parseoni", "Narkhed", "Katol"),
            "Nagpur Urban" to listOf("Nagpur City", "Kamptee", "Hingna", "Umred"),
            "Ramtek" to listOf("Ramtek", "Mansar", "Saoner"),
            "Umred" to listOf("Umred", "Bhiwapur", "Kuhi")
        ),
        "Thane" to mapOf(
            "Thane" to listOf("Thane", "Kalyan", "Dombivli", "Bhiwandi", "Ambernath"),
            "Kalyan" to listOf("Kalyan", "Ulhasnagar", "Shahad", "Ambivli"),
            "Bhiwandi" to listOf("Bhiwandi", "Wada", "Vasai", "Virar"),
            "Murbad" to listOf("Murbad", "Karjat", "Khopoli")
        ),
        "Raigad" to mapOf(
            "Alibag" to listOf("Alibag", "Mandwa", "Rewas", "Nagothane"),
            "Panvel" to listOf("Panvel", "Uran", "Karjat", "Khopoli"),
            "Pen" to listOf("Pen", "Roha", "Sudhagad"),
            "Murud" to listOf("Murud", "Shrivardhan", "Mhasla")
        )
    )

    private var selectedDistrict: String = ""
    private var selectedTaluka: String = ""
    private var selectedVillage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupervisorRegisterAshaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        HeaderUtils.setupRoleInHeader(this)
        // Initialize TokenManager
        TokenManager.init(this)

        requestAudioPermission()
        // Attach voice helper only to fields which should have mic icons (previously only full name)
        voiceHelper = VoiceInputHelper(this)
        voiceHelper.attachVoiceToEditText(binding.etFullName)

        setupSpinners()
        setupClickListeners()

        // new: setup password toggle and criteria
        setupPasswordToggle()
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
        }
    }

    private fun setupSpinners() {
        // Setup District Spinner
        val districts = listOf("Select District") + districtTalukaVillageData.keys.toList()
        val districtAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, districts)
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDistrict.adapter = districtAdapter

        binding.spinnerDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    selectedDistrict = districts[position]
                    setupTalukaSpinner(selectedDistrict)
                } else {
                    selectedDistrict = ""
                    clearTalukaSpinner()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedDistrict = ""
                clearTalukaSpinner()
            }
        }
    }

    private fun setupTalukaSpinner(district: String) {
        val talukas = districtTalukaVillageData[district]?.keys?.toList() ?: emptyList()
        val talukaList = listOf("Select Taluka") + talukas
        val talukaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, talukaList)
        talukaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTaluka.adapter = talukaAdapter
        binding.spinnerTaluka.isEnabled = true

        binding.spinnerTaluka.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    selectedTaluka = talukaList[position]
                    setupVillageSpinner(selectedDistrict, selectedTaluka)
                } else {
                    selectedTaluka = ""
                    clearVillageSpinner()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedTaluka = ""
                clearVillageSpinner()
            }
        }
    }

    private fun setupVillageSpinner(district: String, taluka: String) {
        val villages = districtTalukaVillageData[district]?.get(taluka) ?: emptyList()
        val villageList = listOf("Select Village") + villages
        val villageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, villageList)
        villageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVillage.adapter = villageAdapter
        binding.spinnerVillage.isEnabled = true

        binding.spinnerVillage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedVillage = if (position > 0) villageList[position] else ""
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedVillage = ""
            }
        }
    }

    private fun clearTalukaSpinner() {
        binding.spinnerTaluka.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Select Taluka"))
        binding.spinnerTaluka.isEnabled = false
        selectedTaluka = ""
        clearVillageSpinner()
    }

    private fun clearVillageSpinner() {
        binding.spinnerVillage.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Select Village"))
        binding.spinnerVillage.isEnabled = false
        selectedVillage = ""
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnRegister.setOnClickListener {
            registerAsha()
        }
    }

    private fun registerAsha() {
        val fullName = binding.etFullName.text.toString().trim()
        val phoneNo = binding.etPhoneNo.text.toString().trim()
        val state = "Maharashtra"
        val district = selectedDistrict
        val taluka = selectedTaluka
        val village = selectedVillage
        val age = binding.etAge.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        val token = TokenManager.getToken()
        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Auth token not found")
            return
        }

        Log.d(TAG, "=== REGISTRATION REQUEST DEBUG ===")
        Log.d(TAG, "Token exists: ${token.isNotEmpty()}")
        Log.d(TAG, "Token preview: ${token.take(30)}...")
        Log.d(TAG, "Request data - Name: $fullName, Phone: $phoneNo, State: $state, District: $district, Taluka: $taluka, Village: $village")

        if (validateInputs(fullName, phoneNo, state, district, taluka, village, age, password, confirmPassword)) {
            // Disable button while processing
            binding.btnRegister.isEnabled = false
            binding.btnRegister.text = "Registering..."
            
            val registerRequest = RegisterAshaRequest(
                name = fullName,
                password = password,
                phone = phoneNo,
                village = village,
                district = district,
                taluka = taluka,
                profilePic = null
            )

            Log.d(TAG, "Request object: $registerRequest")

            val authHeader = "Bearer $token"
            val call = ApiClient.retrofit.registerAsha(authHeader, registerRequest)
            
            Log.d(TAG, "Request URL: ${call.request().url}")
            Log.d(TAG, "Request method: ${call.request().method}")
            Log.d(TAG, "Request headers: ${call.request().headers}")

            call.enqueue(object : Callback<RegisterAshaResponse> {
                override fun onResponse(call: Call<RegisterAshaResponse>, response: Response<RegisterAshaResponse>) {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                    
                    Log.d(TAG, "=== RESPONSE DEBUG ===")
                    Log.d(TAG, "Response code: ${response.code()}")
                    Log.d(TAG, "Response message: ${response.message()}")
                    Log.d(TAG, "Response headers: ${response.headers()}")
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        Log.d(TAG, "Registration successful: $responseBody")
                        Toast.makeText(this@RegisterAshaActivity, "ASHA registered successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            Log.e(TAG, "Registration failed - Code: ${response.code()}")
                            Log.e(TAG, "Error body: $errorBody")
                            
                            when (response.code()) {
                                400 -> Toast.makeText(this@RegisterAshaActivity, "Invalid input: $errorBody", Toast.LENGTH_LONG).show()
                                403 -> Toast.makeText(this@RegisterAshaActivity, "Not authorized: Only supervisors can register ASHA", Toast.LENGTH_LONG).show()
                                404 -> Toast.makeText(this@RegisterAshaActivity, "Supervisor profile not found", Toast.LENGTH_LONG).show()
                                409 -> Toast.makeText(this@RegisterAshaActivity, "Phone number already registered", Toast.LENGTH_LONG).show()
                                500 -> Toast.makeText(this@RegisterAshaActivity, "Server error. Please try again.", Toast.LENGTH_LONG).show()
                                else -> Toast.makeText(this@RegisterAshaActivity, "Failed: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing error body: ${e.message}", e)
                            Toast.makeText(this@RegisterAshaActivity, "Registration failed: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<RegisterAshaResponse>, t: Throwable) {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                    Log.e(TAG, "=== API FAILURE ===", t)
                    Log.e(TAG, "Error message: ${t.message}")
                    Log.e(TAG, "Error cause: ${t.cause}")
                    Toast.makeText(this@RegisterAshaActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun setupPasswordToggle() {
        // Toggle for password field
        binding.etPassword.setOnTouchListener { v, event ->
            handleDrawableToggle(binding.etPassword, event)
        }
        // Toggle for confirm password field
        binding.etConfirmPassword.setOnTouchListener { v, event ->
            handleDrawableToggle(binding.etConfirmPassword, event)
        }
    }

    private fun handleDrawableToggle(editText: android.widget.EditText, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val drawableEnd = 2 // index for drawableEnd in getCompoundDrawablesRelative()
            val drawables = editText.compoundDrawablesRelative
            val drawable = drawables[drawableEnd] ?: return false
            // check if touch is within drawable bounds (right side)
            val touchX = event.x
            val width = editText.width
            val paddingRight = editText.paddingEnd
            val drawableWidth = drawable.intrinsicWidth
            if (touchX >= (width - paddingRight - drawableWidth)) {
                // toggle transformation
                val isPasswordShown = editText.transformationMethod == null
                if (isPasswordShown) {
                    // hide
                    editText.transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    // show
                    editText.transformationMethod = null
                }
                // move cursor to end
                editText.setSelection(editText.text?.length ?: 0)
                return true
            }
        }
        return false
    }

    // new: strong password validator
    private fun isStrongPassword(password: String): Boolean {
        if (password.length < 8) return false
        // at least one digit, one lower, one upper, one special char, no whitespace
        val strongRegex = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!()\\-\\[\\]{};:'\"\\\\|,<.>/?_`~])(?=\\S+\$).{8,}\$")
        return strongRegex.containsMatchIn(password)
    }

    private fun validateInputs(
        fullName: String,
        phoneNo: String,
        state: String,
        district: String,
        taluka: String,
        village: String,
        age: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            fullName.isEmpty() -> {
                binding.etFullName.error = "Full Name is required"
                binding.etFullName.requestFocus()
                false
            }
            phoneNo.isEmpty() -> {
                binding.etPhoneNo.error = "Phone Number is required"
                binding.etPhoneNo.requestFocus()
                false
            }
            phoneNo.length != 10 -> {
                binding.etPhoneNo.error = "Phone Number must be 10 digits"
                binding.etPhoneNo.requestFocus()
                false
            }
            district.isEmpty() -> {
                Toast.makeText(this, "Please select a district", Toast.LENGTH_SHORT).show()
                false
            }
            taluka.isEmpty() -> {
                Toast.makeText(this, "Please select a taluka", Toast.LENGTH_SHORT).show()
                false
            }
            village.isEmpty() -> {
                Toast.makeText(this, "Please select a village", Toast.LENGTH_SHORT).show()
                false
            }
            age.isEmpty() -> {
                binding.etAge.error = "Age is required"
                binding.etAge.requestFocus()
                false
            }
            age.toIntOrNull() == null || age.toInt() < 18 -> {
                binding.etAge.error = "Age must be a valid number and at least 18"
                binding.etAge.requestFocus()
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password is required"
                binding.etPassword.requestFocus()
                false
            }
            password.length < 6 -> {
                // keep previous quick length check but prefer strong check below
                binding.etPassword.error = "Password must be at least 6 characters"
                binding.etPassword.requestFocus()
                false
            }
            !isStrongPassword(password) -> {
                binding.etPassword.error = "Weak password"
                Toast.makeText(this, "Password must be at least 8 chars, include uppercase, lowercase, digit and special character.", Toast.LENGTH_LONG).show()
                binding.etPassword.requestFocus()
                false
            }
            confirmPassword.isEmpty() -> {
                binding.etConfirmPassword.error = "Confirm Password is required"
                binding.etConfirmPassword.requestFocus()
                false
            }
            password != confirmPassword -> {
                binding.etConfirmPassword.error = "Passwords do not match"
                binding.etConfirmPassword.requestFocus()
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    // Clean up any SpeechRecognizer resources associated with this activity
    override fun onDestroy() {
        voiceHelper.destroy()
        super.onDestroy()
    }
}
