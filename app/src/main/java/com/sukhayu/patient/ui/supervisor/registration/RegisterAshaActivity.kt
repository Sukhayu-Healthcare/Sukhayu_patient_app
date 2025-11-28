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
    private val SUPERVISOR_ID = "SUP001" // Fixed supervisor ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupervisorRegisterAshaBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val aadharNumber = binding.etAadharNumber.text.toString().trim()
        val age = binding.etAge.text.toString().trim()
        val password = aadharNumber // Using aadhar as password or add etPassword to layout

        val token = TokenManager.getToken()
        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Auth token not found")
            return
        }

        Log.d(TAG, "Using Supervisor ID: $SUPERVISOR_ID")
        Log.d(TAG, "Using Bearer Token: ${token.take(20)}...")

        if (validateInputs(fullName, village, phoneNo, district, taluka, aadharNumber, age)) {
            Log.d(TAG, "Request data - Name: $fullName, Phone: $phoneNo, Village: $village")
            
            val registerRequest = RegisterAshaRequest(
                name = fullName,
                password = password,
                phone = phoneNo,
                village = village,
                district = district,
                taluka = taluka,
                profilePic = null,
                supervisorId = SUPERVISOR_ID
            )

            ApiClient.retrofit.registerAsha("Bearer $token", registerRequest).enqueue(object : Callback<RegisterAshaResponse> {
                override fun onResponse(call: Call<RegisterAshaResponse>, response: Response<RegisterAshaResponse>) {
                    Log.d(TAG, "Response code: ${response.code()}")
                    
                    if (response.isSuccessful) {
                        Log.d(TAG, "Registration successful: ${response.body()}")
                        Toast.makeText(this@RegisterAshaActivity, "ASHA registered successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            Log.e(TAG, "Registration failed - Code: ${response.code()}, Error: $errorBody")
                            Toast.makeText(this@RegisterAshaActivity, "Failed: $errorBody", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing error body: ${e.message}")
                            Toast.makeText(this@RegisterAshaActivity, "Registration failed: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<RegisterAshaResponse>, t: Throwable) {
                    Log.e(TAG, "Network error: ${t.message}", t)
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
        aadharNumber: String,
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
