package com.sukhayu.patient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etOtp = findViewById<EditText>(R.id.etOtp)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val otp = etOtp.text.toString().trim()

            // Dummy validation
            if (username == "Dummy Patient" && otp == "123456") {
                // Navigate to Dashboard
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("USER_username", username)
                startActivity(intent)
                finish()  // Optional: removes login screen from back stack
            } else {
                Toast.makeText(
                    this,
                    "Invalid credentials. Use Dummy Patient / 123456",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
