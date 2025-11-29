package com.sukhayu.patient.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.*
import com.sukhayu.patient.ui.asha.dashboard.AshaDashboardActivity
import com.sukhayu.patient.ui.dashboard.DashboardActivity
import com.sukhayu.patient.ui.supervisor.dashboard.SupervisorHomeActivity
import com.sukhayu.patient.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize TokenManager
        TokenManager.init(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etOtp)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Set phone number field to number input with 10 digit limit
        etUsername.inputType = InputType.TYPE_CLASS_NUMBER
        etUsername.filters = arrayOf(InputFilter.LengthFilter(10))

        btnLogin.setOnClickListener {
            val phoneNumber = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (phoneNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter phone number & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(phoneNumber, password)

            ApiClient.retrofit.login(request).enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (!response.isSuccessful || response.body() == null) {
                        Toast.makeText(this@LoginActivity, "Invalid login response", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val json = gson.toJson(response.body())
                    val role = response.body()?.get("role")?.toString()?.lowercase()

                    when (role) {

                        /** PATIENT LOGIN **/
                        "patient" -> {
                            val parsed = gson.fromJson(json, LoginResponsePatient::class.java)

                            savePatientLogin(parsed)
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        }

                        /** ASHA / SUPERVISOR LOGIN **/
                        "asha", "supervisor" -> {
                            val parsed = gson.fromJson(json, LoginResponseAshaOrSupervisor::class.java)

                            saveAshaOrSupervisorLogin(parsed)

                            if (role == "supervisor") {
                                startActivity(Intent(this@LoginActivity, SupervisorHomeActivity::class.java))
                            } else {
                                startActivity(Intent(this@LoginActivity, AshaDashboardActivity::class.java))
                            }
                        }

                        else -> {
                            Toast.makeText(this@LoginActivity, "Unknown role", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun savePatientLogin(data: LoginResponsePatient) {
        getSharedPreferences("auth", MODE_PRIVATE).edit().apply {
            putString("token", data.token)
            putString("role", data.role)
            putString("user_id", data.patient.id)
            putString("user_name", data.patient.name)
            putString("user_phone", data.patient.phone)
            putString("supreme_id", data.patient.supreme_id ?: "")
            apply()
        }

        // Also update TokenManager
        TokenManager.saveToken(
            token = data.token,
            userId = data.patient.id,
            supremeId = data.patient.supreme_id ?: "",
            role = data.role
        )
    }

    private fun saveAshaOrSupervisorLogin(data: LoginResponseAshaOrSupervisor) {
        getSharedPreferences("auth", MODE_PRIVATE).edit().apply {
            putString("token", data.token)
            putString("role", data.role)
            putString("user_id", data.user.id)
            putString("user_name", data.user.name)
            putString("user_phone", data.user.phone)
            apply()
        }

        // Also update TokenManager
        TokenManager.saveToken(
            token = data.token,
            userId = data.user.id,
            supremeId = "",
            role = data.role
        )
    }
}
