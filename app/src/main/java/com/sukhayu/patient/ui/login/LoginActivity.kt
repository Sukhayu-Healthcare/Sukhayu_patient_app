package com.sukhayu.patient.ui.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.data.remote.*
import com.sukhayu.patient.data.repository.PatientRepository
import com.sukhayu.patient.ui.asha.dashboard.AshaDashboardActivity
import com.sukhayu.patient.ui.dashboard.DashboardActivity
import com.sukhayu.patient.ui.supervisor.dashboard.SupervisorHomeActivity
import com.sukhayu.patient.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private val gson = Gson()

    // 🔹 Auto-login if already logged in
    override fun onStart() {
        super.onStart()

        // Ensure TokenManager is initialized
        TokenManager.init(this)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val role = prefs.getString("role", null)?.lowercase()

        // If token & role exist → skip login screen
        if (!token.isNullOrEmpty() && !role.isNullOrEmpty()) {
            when (role) {
                "asha" -> {
                    startActivity(Intent(this, AshaDashboardActivity::class.java))
                }
                "supervisor" -> {
                    startActivity(Intent(this, SupervisorHomeActivity::class.java))
                }
                "patient" -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                }
            }
            finish() // don’t allow back press to return to login
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize TokenManager (safe to call again)
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
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid login response",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val json = gson.toJson(response.body())
                    val role = response.body()?.get("role")?.toString()?.lowercase()

                    when (role?.lowercase()) {

                        /** PATIENT LOGIN **/
                        "patient" -> {
                            val parsed = gson.fromJson(json, LoginResponsePatient::class.java)

                            savePatientLogin(parsed)

                            // guard cache so it can't crash the app
                            try {
                                cachePatient(parsed.patient)
                            } catch (e: Exception) {
                                Log.e("LoginActivity", "Failed to cache patient locally", e)
                            }

                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    DashboardActivity::class.java
                                )
                            )
                            finish()
                        }

                        /** ASHA / SUPERVISOR LOGIN **/
                        "asha", "supervisor" -> {
                            val parsed =
                                gson.fromJson(json, LoginResponseAshaOrSupervisor::class.java)

                            saveAshaOrSupervisorLogin(parsed)

                            if (role == "supervisor") {
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        SupervisorHomeActivity::class.java
                                    )
                                )
                            } else {
                                Log.d(
                                    "LoginActivity",
                                    "ASHA login branch reached, starting sync..."
                                )
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val db =
                                            AshaLocalDatabase.getInstance(applicationContext)
                                        val repo = PatientRepository(db, ApiClient.retrofit)
                                        repo.syncPatientsFromServer(parsed.token)
                                    } catch (e: Exception) {
                                        Log.e(
                                            "LoginActivity",
                                            "Failed to sync patients",
                                            e
                                        )
                                        runOnUiThread {
                                            Toast.makeText(
                                                this@LoginActivity,
                                                "Could not sync patients, working in offline mode.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                }
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        AshaDashboardActivity::class.java
                                    )
                                )
                            }
                            finish()
                        }

                        else -> {
                            Toast.makeText(
                                this@LoginActivity,
                                "Unknown role",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
        }
    }

    private fun savePatientLogin(data: LoginResponsePatient) {
        // Save in SharedPreferences
        getSharedPreferences("auth", MODE_PRIVATE).edit().apply {
            putString("token", data.token)
            putString("role", data.role)
            putString("role_display", getRoleDisplayName(data.role))

            // Prefer patientId, fall back to userId, otherwise empty
            putString(
                "user_id",
                data.patient.patientId ?: data.patient.userId ?: ""
            )
            putString("user_name", data.patient.name ?: "")
            putString("user_phone", data.patient.phone ?: "")

            // supreme_id is now supremeId in the model
            putString("supreme_id", data.patient.supremeId ?: "")

            apply()
        }

        // Also update TokenManager
        TokenManager.saveToken(
            token = data.token,
            userId = data.patient.patientId
                ?: data.patient.userId
                ?: data.patient.phone
                ?: "unknown",
            supremeId = data.patient.supremeId ?: "",
            role = data.role
        )
    }

    private fun saveAshaOrSupervisorLogin(data: LoginResponseAshaOrSupervisor) {
        // Save in SharedPreferences
        getSharedPreferences("auth", MODE_PRIVATE).edit().apply {
            putString("token", data.token)
            putString("role", data.role)
            putString("role_display", getRoleDisplayName(data.role))
            putString("user_id", data.user.id ?: "")
            putString("user_name", data.user.name ?: "")
            putString("user_phone", data.user.phone ?: "")
            apply()
        }

        // Also save into TokenManager so ViewModels can read it
        TokenManager.saveToken(
            token = data.token,
            userId = data.user.id ?: data.user.phone ?: "unknown",
            supremeId = "",              // ASHA doesn’t have a supreme_id here
            role = data.role
        )
    }

    private fun cachePatient(patient: PatientInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AshaLocalDatabase.getInstance(applicationContext)
                val dao = db.patientDao()

                // Build a safe PatientEntity – no nulls in non-null params
                val entity = PatientEntity(
                    id = patient.patientId
                        ?: patient.userId
                        ?: patient.phone
                        ?: System.currentTimeMillis().toString(),
                    name = patient.name ?: "Unknown",
                    phone = patient.phone,
                    gender = null,
                    weightKg = null,
                    supremeId = patient.supremeId,
                    age = null
                )

                dao.insertOrUpdate(entity)
                Log.d("LoginActivity", "Cached patient locally: $entity")
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error while caching patient locally", e)
            }
        }
    }

    private fun calculateAge(dob: String): Int? {
        return try {
            val formatter =
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val birthDate = formatter.parse(dob) ?: return null
            val diff = java.util.Calendar.getInstance().time.time - birthDate.time
            (diff / (1000L * 60 * 60 * 24 * 365)).toInt()
        } catch (e: Exception) {
            null
        }
    }

    private fun getRoleDisplayName(role: String): String {
        return when (role.lowercase()) {
            "patient" -> "Patient"
            "asha" -> "ASHA Worker"
            "supervisor" -> "ASHA Supervisor"
            else -> role.replaceFirstChar { it.uppercase() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
