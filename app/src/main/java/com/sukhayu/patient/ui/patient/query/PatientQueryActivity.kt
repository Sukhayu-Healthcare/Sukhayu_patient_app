package com.sukhayu.patient.ui.patient.query

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.PatientQueryRequest
import com.sukhayu.patient.data.remote.PatientQueryResponse
import com.sukhayu.patient.ui.dashboard.DashboardActivity
import com.sukhayu.patient.utils.HeaderUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class PatientQueryActivity : AppCompatActivity() {

    private val TAG = "PatientQueryActivity"
    private val AUDIO_REQUEST_CODE = 100

    private lateinit var spinnerDisease: Spinner
    private lateinit var etDocType: EditText
    private lateinit var etQueryText: EditText
    private lateinit var btnRecordVoice: Button
    private lateinit var btnPlayVoice: Button
    private lateinit var btnClearVoice: Button
    private lateinit var btnSubmitQuery: Button
    private lateinit var btnCancel: Button
    private lateinit var tvVoiceStatus: TextView

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var voiceFilePath: String? = null
    private var isRecording = false

    // Disease zones mapping
    private val diseaseZones = mapOf(
        "Viral Fever" to "yellow",
        "Acid Reflux" to "yellow",
        "Migraine" to "yellow",
        "Pregnancy Complications" to "orange",
        "Severe Dehydration" to "orange",
        "Snake Bite" to "orange"
    )

    private val doctorTypeMap = mapOf(
        "yellow" to "Community Health Officer",
        "orange" to "Medical Officer"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_query)

        HeaderUtils.setupRoleInHeader(this)

        initializeViews()
        setupDiseaseSpinner()
        setupListeners()
        requestAudioPermissions()
    }

    private fun initializeViews() {
        spinnerDisease = findViewById(R.id.spinnerDisease)
        etDocType = findViewById(R.id.etDocType)
        etQueryText = findViewById(R.id.etQueryText)
        btnRecordVoice = findViewById(R.id.btnRecordVoice)
        btnPlayVoice = findViewById(R.id.btnPlayVoice)
        btnClearVoice = findViewById(R.id.btnClearVoice)
        btnSubmitQuery = findViewById(R.id.btnSubmitQuery)
        btnCancel = findViewById(R.id.btnCancel)
        tvVoiceStatus = findViewById(R.id.tvVoiceStatus)
    }

    private fun setupDiseaseSpinner() {
        val diseases = arrayOf(
            "Select Health Issue",
            "Viral Fever",
            "Acid Reflux",
            "Migraine",
            "Pregnancy Complications",
            "Severe Dehydration",
            "Snake Bite"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, diseases)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDisease.adapter = adapter

        spinnerDisease.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedDisease = diseases[position]
                if (selectedDisease != "Select Health Issue") {
                    val zone = diseaseZones[selectedDisease]
                    val doctorType = doctorTypeMap[zone] ?: "Not Available"
                    etDocType.setText(doctorType)
                    Log.d(TAG, "Disease selected: $selectedDisease, Zone: $zone, Doctor: $doctorType")
                } else {
                    etDocType.setText("")
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                etDocType.setText("")
            }
        })
    }

    private fun setupListeners() {
        btnRecordVoice.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }

        btnPlayVoice.setOnClickListener {
            if (voiceFilePath != null) {
                playVoiceRecording()
            }
        }

        btnClearVoice.setOnClickListener {
            clearVoiceRecording()
        }

        btnSubmitQuery.setOnClickListener {
            submitQuery()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                AUDIO_REQUEST_CODE
            )
        }
    }

    private fun startRecording() {
        try {
            voiceFilePath = "${externalCacheDir?.absolutePath}/query_voice_${System.currentTimeMillis()}.m4a"
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(voiceFilePath)
                prepare()
                start()
            }
            
            isRecording = true
            btnRecordVoice.text = "⏹️ Stop Recording"
            tvVoiceStatus.text = "Recording in progress..."
            tvVoiceStatus.setTextColor(getColor(android.R.color.holo_orange_dark))
            Log.d(TAG, "Recording started: $voiceFilePath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            btnRecordVoice.text = "🎤 Record Voice"
            btnPlayVoice.isEnabled = true
            btnClearVoice.isEnabled = true
            tvVoiceStatus.text = "Voice recorded successfully ✓"
            tvVoiceStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            Log.d(TAG, "Recording stopped: $voiceFilePath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            Toast.makeText(this, "Failed to stop recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playVoiceRecording() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(voiceFilePath)
                prepare()
                start()
            }
            tvVoiceStatus.text = "Playing voice message..."
            tvVoiceStatus.setTextColor(getColor(android.R.color.holo_blue_dark))
            Log.d(TAG, "Playing voice: $voiceFilePath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play voice", e)
            Toast.makeText(this, "Failed to play voice recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearVoiceRecording() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            if (voiceFilePath != null) {
                File(voiceFilePath!!).delete()
            }
            voiceFilePath = null
            btnPlayVoice.isEnabled = false
            btnClearVoice.isEnabled = false
            btnRecordVoice.text = "🎤 Record Voice"
            tvVoiceStatus.text = "No voice recorded"
            tvVoiceStatus.setTextColor(getColor(android.R.color.darker_gray))
            Log.d(TAG, "Voice recording cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear voice", e)
        }
    }

    private fun submitQuery() {
        val disease = spinnerDisease.selectedItem.toString().trim()
        val docType = etDocType.text.toString().trim()
        val queryText = etQueryText.text.toString().trim()

        // Validation
        if (disease == "Select Health Issue") {
            Toast.makeText(this, "Please select a health issue", Toast.LENGTH_SHORT).show()
            return
        }

        if (queryText.isEmpty()) {
            etQueryText.error = "Please enter your query"
            return
        }

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val userIdStr = prefs.getString("user_id", "") ?: ""
        val patientId = userIdStr.toIntOrNull() ?: 0
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "📝 SUBMITTING PATIENT QUERY")
        Log.d(TAG, "Patient ID: $patientId")
        Log.d(TAG, "Disease: $disease")
        Log.d(TAG, "Doctor Type: $docType")
        Log.d(TAG, "Query: $queryText")
        Log.d(TAG, "Voice File: $voiceFilePath")
        Log.d(TAG, "Auth Token: $token")
        Log.d(TAG, "========================================")

        // Validate token
        if (token.isEmpty()) {
            Log.e(TAG, "❌ NO AUTH TOKEN FOUND")
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate patient ID
        if (patientId == 0) {
            Log.e(TAG, "❌ INVALID PATIENT ID: $patientId")
            Toast.makeText(this, "Patient ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val queryRequest = PatientQueryRequest(
            asha_id = null,
            text = queryText,
            voice_url = voiceFilePath,
            disease = disease,
            doc = docType,
            doc_id = null
        )

        Log.d(TAG, "📤 Request Body: $queryRequest")
        Log.d(TAG, "🔗 API Endpoint: query/patient")
        Log.d(TAG, "🔐 Authorization: Bearer $token")

        btnSubmitQuery.isEnabled = false
        btnSubmitQuery.text = "Submitting..."

        ApiClient.retrofit.submitPatientQuery("Bearer $token",queryRequest)
            .enqueue(object : Callback<PatientQueryResponse> {
                override fun onResponse(
                    call: Call<PatientQueryResponse>,
                    response: Response<PatientQueryResponse>
                ) {
                    Log.d(TAG, "========================================")
                    Log.d(TAG, "📡 BACKEND RESPONSE RECEIVED")
                    Log.d(TAG, "Response Code: ${response.code()}")
                    Log.d(TAG, "Is Successful: ${response.isSuccessful}")
                    Log.d(TAG, "Response Headers: ${response.headers()}")
                    Log.d(TAG, "Response Body: ${response.body()}")
                    Log.d(TAG, "Response Raw: ${response.raw()}")
                    Log.d(TAG, "========================================")

                    if (response.isSuccessful && response.body() != null) {
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "✅ QUERY SUBMITTED SUCCESSFULLY")
                        Log.d(TAG, "Query ID: ${response.body()?.data?.query_id}")
                        Log.d(TAG, "Message: ${response.body()?.message}")
                        Log.d(TAG, "Status: ${response.body()?.data?.query_status}")
                        Log.d(TAG, "========================================")

                        Toast.makeText(
                            this@PatientQueryActivity,
                            "Query submitted successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this@PatientQueryActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "========================================")
                        Log.e(TAG, "❌ BACKEND RETURNED ERROR")
                        Log.e(TAG, "Status Code: ${response.code()}")
                        Log.e(TAG, "Error Body: $errorBody")
                        Log.e(TAG, "========================================")
                        
                        Toast.makeText(
                            this@PatientQueryActivity,
                            "Error: ${response.code()} - Failed to submit query",
                            Toast.LENGTH_SHORT
                        ).show()
                        btnSubmitQuery.isEnabled = true
                        btnSubmitQuery.text = "Submit"
                    }
                }

                override fun onFailure(call: Call<PatientQueryResponse>, t: Throwable) {
                    Log.e(TAG, "========================================")
                    Log.e(TAG, "❌ NETWORK ERROR SUBMITTING QUERY")
                    Log.e(TAG, "Error Message: ${t.message}")
                    Log.e(TAG, "Error Type: ${t.javaClass.simpleName}")
                    Log.e(TAG, "Error Cause: ${t.cause}")
                    Log.e(TAG, "========================================")
                    t.printStackTrace()

                    Toast.makeText(
                        this@PatientQueryActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnSubmitQuery.isEnabled = true
                    btnSubmitQuery.text = "Submit"
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaPlayer?.release()
        mediaRecorder = null
        mediaPlayer = null
    }
}
