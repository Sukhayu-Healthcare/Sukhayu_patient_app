package com.sukhayu.patient.ui.supervisor.registration

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.RegisterAshaRequest
import com.sukhayu.patient.data.remote.RegisterAshaResponse
import com.sukhayu.patient.databinding.ActivitySupervisorRegisterAshaBinding
import com.sukhayu.patient.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterAshaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupervisorRegisterAshaBinding
    private val TAG = "RegisterAshaActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupervisorRegisterAshaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize TokenManager
        TokenManager.init(this)

        setupClickListeners()
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
        val village = binding.etVillage.text.toString().trim()
        val phoneNo = binding.etPhoneNo.text.toString().trim()
        val district = binding.etDistrict.text.toString().trim()
        val taluka = binding.etTaluka.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val age = binding.etAge.text.toString().trim()

        val token = TokenManager.getToken()
        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Auth token not found")
            return
        }

        Log.d(TAG, "=== REGISTRATION REQUEST DEBUG ===")
        Log.d(TAG, "Token exists: ${token.isNotEmpty()}")
        Log.d(TAG, "Token preview: ${token.take(30)}...")
        Log.d(TAG, "Request data - Name: $fullName, Phone: $phoneNo, Village: $village, District: $district, Taluka: $taluka")

        if (validateInputs(fullName, village, phoneNo, district, taluka, password, age)) {
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
                // Remove supervisorId - backend gets it from token
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

    private fun validateInputs(
        fullName: String,
        village: String,
        phoneNo: String,
        district: String,
        taluka: String,
        password: String,
        age: String
    ): Boolean {
        return when {
            fullName.isEmpty() -> {
                binding.etFullName.error = "Full Name is required"
                false
            }
            village.isEmpty() -> {
                binding.etVillage.error = "Village is required"
                false
            }
            phoneNo.isEmpty() -> {
                binding.etPhoneNo.error = "Phone Number is required"
                false
            }
            phoneNo.length != 10 -> {
                binding.etPhoneNo.error = "Phone Number must be 10 digits"
                false
            }
            district.isEmpty() -> {
                binding.etDistrict.error = "District is required"
                false
            }
            taluka.isEmpty() -> {
                binding.etTaluka.error = "Taluka is required"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password is required"
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Password must be at least 6 characters"
                false
            }
            age.isEmpty() -> {
                binding.etAge.error = "Age is required"
                false
            }
            age.toIntOrNull() == null || age.toInt() < 18 -> {
                binding.etAge.error = "Age must be a valid number and at least 18"
                false
            }
            else -> true
        }
    }
}
