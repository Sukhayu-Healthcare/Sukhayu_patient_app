package com.sukhayu.patient.ui.login 

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.sukhayu.patient.R
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.LoginRequest
import com.sukhayu.patient.model.LoginResponse
import com.sukhayu.patient.ui.dashboard.DashboardActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

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
                            getSharedPreferences("auth", MODE_PRIVATE)
                                .edit()
                                .putString("token", body.token)
                                .apply()
                            Toast.makeText(
                                this@LoginActivity,
                                "Logged in using API",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                            return
                        }

                        // ---------------------------
                        //  FALLBACK DUMMY LOGIN
                        // ---------------------------
                        else if (username == "Dummy Patient" && password == "123456") {

                            Toast.makeText(
                                this@LoginActivity,
                                "Logged in using dummy account",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                            return
                        }

                        // ---------------------------
                        //  BOTH FAILED
                        // ---------------------------
                        else{
                        Toast.makeText(
                            this@LoginActivity,
                            body?.message ?: "Login failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {

                        // ---------------------------
                        //  API DOWN → USE DUMMY LOGIN
                        // ---------------------------
                        if (username == "Dummy Patient" && password == "123456") {

                            Toast.makeText(
                                this@LoginActivity,
                                "API unreachable — using dummy login",
                                Toast.LENGTH_LONG
                            ).show()

                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                            return
                        }

                        Toast.makeText(
                            this@LoginActivity,
                            "Network error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }
}
