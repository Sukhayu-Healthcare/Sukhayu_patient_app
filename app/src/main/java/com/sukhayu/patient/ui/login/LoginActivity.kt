package com.sukhayu.patient.ui.login 

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.sukhayu.patient.R
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.LoginRequest
import com.sukhayu.patient.data.remote.LoginResponseAshaOrSupervisor
import com.sukhayu.patient.data.remote.LoginResponsePatient
import com.sukhayu.patient.ui.dashboard.DashboardActivity
import com.sukhayu.patient.ui.supervisor.dashboard.SupervisorHomeActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    companion object {
        const val DUMMY_PATIENT_USERNAME = "patient"
        const val DUMMY_PATIENT_PASSWORD = "123456"
        const val DUMMY_SUPERVISOR_USERNAME = "supervisor"
        const val DUMMY_SUPERVISOR_PASSWORD = "123456"
        const val DUMMY_ASHA_USERNAME = "asha"
        const val DUMMY_ASHA_PASSWORD = "123456"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etOtp)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter username & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isDummyCredentialsValid(username, password)) {
                val role = getDummyRoleByCredentials(username)
                getSharedPreferences("auth", MODE_PRIVATE)
                    .edit()
                    .putString("token", "dummy_token_$role")
                    .putString("role", role)
                    .apply()

                Toast.makeText(
                    this@LoginActivity,
                    "Logged in as $role (dummy)",
                    Toast.LENGTH_SHORT
                ).show()

                routeUserByRole(role)
                finish()
                return@setOnClickListener
            }

            val request = LoginRequest(
                phone = username,
                password = password
            )

            // Try patient login first
            ApiClient.retrofit.loginPatient(request)
                .enqueue(object : Callback<LoginResponsePatient> {
                    override fun onResponse(
                        call: Call<LoginResponsePatient>,
                        response: Response<LoginResponsePatient>
                    ) {
                        if (response.isSuccessful && response.body()?.token != null) {
                            val body = response.body()!!
                            Log.d("LoginActivity", "Patient login successful: ${body.message}")
                            
                            getSharedPreferences("auth", MODE_PRIVATE).edit().apply {
                                putString("token", body.token)
                                putString("role", body.role)
                                putString("user_id", body.patient.id)
                                putString("user_name", body.patient.name)
                                putString("user_phone", body.patient.phone)
                                putString("supreme_id", body.patient.supreme_id)
                                apply()
                            }
                            
                            Toast.makeText(
                                this@LoginActivity,
                                body.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                            return
                        }
                        
                        // If patient login fails, try ASHA/Supervisor login
                        loginAsOrSupervisor(request)
                    }

                    override fun onFailure(call: Call<LoginResponsePatient>, t: Throwable) {
                        Log.e("LoginActivity", "Patient login error: ${t.message}")
                        // If patient API fails, try ASHA/Supervisor login
                        loginAsOrSupervisor(request)
                    }
                })
        }
    }

    private fun loginAsOrSupervisor(request: LoginRequest) {
        // Try ASHA login
        ApiClient.retrofit.loginAsha(request)
            .enqueue(object : Callback<LoginResponseAshaOrSupervisor> {
                override fun onResponse(
                    call: Call<LoginResponseAshaOrSupervisor>,
                    response: Response<LoginResponseAshaOrSupervisor>
                ) {
                    if (response.isSuccessful && response.body()?.token != null) {
                        val body = response.body()!!
                        Log.d("LoginActivity", "ASHA login successful: ${body.message}")
                        
                        getSharedPreferences("auth", MODE_PRIVATE).edit().apply {
                            putString("token", body.token)
                            putString("role", body.role)
                            putString("user_id", body.user.id)
                            putString("user_name", body.user.name)
                            putString("user_phone", body.user.phone)
                            apply()
                        }
                        
                        Toast.makeText(
                            this@LoginActivity,
                            body.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        routeUserByRole(body.role)
                        finish()
                        return
                    }
                    
                    // If ASHA fails, try Supervisor login
                    loginSupervisor(request)
                }

                override fun onFailure(call: Call<LoginResponseAshaOrSupervisor>, t: Throwable) {
                    Log.e("LoginActivity", "ASHA login error: ${t.message}")
                    loginSupervisor(request)
                }
            })
    }

    private fun loginSupervisor(request: LoginRequest) {
        ApiClient.retrofit.loginSupervisor(request)
            .enqueue(object : Callback<LoginResponseAshaOrSupervisor> {
                override fun onResponse(
                    call: Call<LoginResponseAshaOrSupervisor>,
                    response: Response<LoginResponseAshaOrSupervisor>
                ) {
                    if (response.isSuccessful && response.body()?.token != null) {
                        val body = response.body()!!
                        Log.d("LoginActivity", "Supervisor login successful: ${body.message}")
                        
                        getSharedPreferences("auth", MODE_PRIVATE).edit().apply {
                            putString("token", body.token)
                            putString("role", body.role)
                            putString("user_id", body.user.id)
                            putString("user_name", body.user.name)
                            putString("user_phone", body.user.phone)
                            apply()
                        }
                        
                        Toast.makeText(
                            this@LoginActivity,
                            body.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        routeUserByRole(body.role)
                        finish()
                        return
                    }
                    
                    Toast.makeText(
                        this@LoginActivity,
                        response.body()?.message ?: "Login failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(call: Call<LoginResponseAshaOrSupervisor>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun isDummyCredentialsValid(username: String, password: String): Boolean {
        return when {
            username == DUMMY_PATIENT_USERNAME && password == DUMMY_PATIENT_PASSWORD -> true
            username == DUMMY_SUPERVISOR_USERNAME && password == DUMMY_SUPERVISOR_PASSWORD -> true
            username == DUMMY_ASHA_USERNAME && password == DUMMY_ASHA_PASSWORD -> true
            else -> false
        }
    }

    private fun getDummyRoleByCredentials(username: String): String {
        return when (username) {
            DUMMY_PATIENT_USERNAME -> "patient"
            DUMMY_SUPERVISOR_USERNAME -> "supervisor"
            DUMMY_ASHA_USERNAME -> "asha"
            else -> "patient"
        }
    }

    private fun routeUserByRole(role: String) {
        val intent = when (role.lowercase()) {
            "patient" -> Intent(this, DashboardActivity::class.java)
            "supervisor" -> Intent(this, SupervisorHomeActivity::class.java)
            "asha" -> Intent(this, DashboardActivity::class.java)
            else -> Intent(this, DashboardActivity::class.java)
        }
        startActivity(intent)
    }
}
