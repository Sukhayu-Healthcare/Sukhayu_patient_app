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
import com.sukhayu.patient.model.LoginResponse
import com.sukhayu.patient.ui.dashboard.DashboardActivity
import com.sukhayu.patient.ui.supervisor.dashboard.SupervisorHomeActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    companion object {
        // Dummy credentials for testing
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

            // Check dummy credentials first and route accordingly
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

            ApiClient.retrofit.loginPatient(request)
                .enqueue(object : Callback<LoginResponse> {

                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        val body = response.body()

                        // ---------------------------
                        //  SUCCESSFUL API LOGIN
                        // ---------------------------
                        if (response.isSuccessful && body?.token != null) {
                            Log.d("response","${response}")
                            getSharedPreferences("auth", MODE_PRIVATE)
                                .edit()
                                .putString("token", body.token)
                                .apply()
                            Toast.makeText(
                                this@LoginActivity,
                                "Logged in",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                            return
                        }

                        // ---------------------------
                        //  BOTH FAILED
                        // ---------------------------
                        else {
                            Toast.makeText(
                                this@LoginActivity,
                                body?.message ?: "Login failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Network error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
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
