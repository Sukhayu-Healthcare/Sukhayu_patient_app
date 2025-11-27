package com.sukhayu.patient.ui.supervisor.registration

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.databinding.ActivitySupervisorRegisterAshaBinding

class RegisterAshaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupervisorRegisterAshaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupervisorRegisterAshaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Safe handling for intent extras
        val role = intent?.getStringExtra("role") ?: "supervisor"

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

        if (validateInputs(fullName, village, phoneNo, district, taluka, aadharNumber, age)) {
            // TODO: Send data to server or save locally
            // Example: callRegisterApi(fullName, village, phoneNo, district, taluka, aadharNumber, age)
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
            aadharNumber.isEmpty() -> {
                binding.etAadharNumber.error = "Aadhar Number is required"
                false
            }
            aadharNumber.length != 12 -> {
                binding.etAadharNumber.error = "Aadhar Number must be 12 digits"
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
