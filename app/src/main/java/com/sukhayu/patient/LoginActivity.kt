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

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            // Dummy validation
            if (email == "patient@example.com" && pass == "password") {
                val i = Intent(this, DashboardActivity::class.java)
                i.putExtra("USER_EMAIL", email)
                startActivity(i)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials. Use patient@example.com / password", Toast.LENGTH_LONG).show()
            }
        }
    }
}
