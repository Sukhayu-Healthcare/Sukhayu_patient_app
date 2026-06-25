package com.sukhayu.patient.ui.patient.appointment

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.AppointmentEntity
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.BookAppointmentRequest
import com.sukhayu.patient.data.remote.Doctor
import com.sukhayu.patient.databinding.ActivityBookAppointmentBinding
import com.sukhayu.patient.utils.HeaderUtils
import com.sukhayu.patient.utils.LocalizableActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
import android.widget.AdapterView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.utils.TtsHelper
import com.sukhayu.patient.utils.ViewTtsHelper
import com.sukhayu.utils.VoiceInputHelper

class BookAppointmentActivity : LocalizableActivity() {

    private lateinit var binding: ActivityBookAppointmentBinding
    private lateinit var db: AshaLocalDatabase
    private var selectedDoctor: Doctor? = null
    private val appointments = mutableListOf<AppointmentEntity>()
    private lateinit var appointmentAdapter: AppointmentAdapter
    private var availableDoctors: List<Doctor> = emptyList()
    private val TAG = "BookAppointmentActivity"

    private lateinit var ttsHelper: TtsHelper

    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        HeaderUtils.setupRoleInHeader(this)
        // Setup language toggle in header
        setupLanguageToggle()
        requestAudioPermission()

        db = AshaLocalDatabase.getInstance(this)

        // Setup RecyclerView
        appointmentAdapter = AppointmentAdapter(appointments) { appointment ->
            deleteAppointment(appointment)
        }
        binding.rvSavedAppointments.apply {
            layoutManager = LinearLayoutManager(this@BookAppointmentActivity)
            adapter = appointmentAdapter
        }

        // Fetch available doctors from API
        fetchAvailableDoctors()

        // Doctor selection
        binding.btnChoSelect.setOnClickListener {
            if (availableDoctors.isNotEmpty()) {
                showDoctorSelector()
            } else {
                Toast.makeText(this, "Fetching doctors...", Toast.LENGTH_SHORT).show()
                fetchAvailableDoctors()
            }
        }

        // Date picker
        binding.etAppointmentDate.setOnClickListener {
            showDatePicker()
        }

        // Time picker
        binding.etAppointmentTime.setOnClickListener {
            showTimePicker()
        }

        // Save appointment
        binding.btnSaveAppointment.setOnClickListener {
            saveAppointment()
        }

        // Load saved appointments
        loadSavedAppointments()

        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
        // Initialize TTS
        ttsHelper = TtsHelper(this)

        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val currentLang = prefs.getString("My_Lang", "en") ?: "en"

        ttsHelper.setLanguage(currentLang)

        // Enable TTS on all TextViews and Buttons
        ViewTtsHelper.attachToAllTextViews(
            findViewById(android.R.id.content),
            ttsHelper
        )
    }

    private fun requestAudioPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
        }
    }

    private fun fetchAvailableDoctors() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        if (token.isEmpty()) {
            Log.e(TAG, "No auth token found")
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Fetching available doctors...")
                val response = ApiClient.retrofit.getAvailableDoctors("Bearer $token")
                
                availableDoctors = response.doctors
                Log.d(TAG, "✅ Fetched ${availableDoctors.size} doctors")
                
                if (availableDoctors.isEmpty()) {
                    Toast.makeText(this@BookAppointmentActivity, "No doctors available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching doctors: ${e.message}", e)
                Toast.makeText(this@BookAppointmentActivity, "Failed to load doctors", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDoctorSelector() {
        val doctorNames = availableDoctors.map { 
            "${it.doc_name} (${it.doc_speciality ?: "General"}) - ${it.doc_phone ?: "N/A"}" 
        }.toTypedArray()
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Select Doctor")
            .setItems(doctorNames) { _, which ->
                selectedDoctor = availableDoctors[which]
                binding.etDoctorName.setText(selectedDoctor?.doc_name ?: "")
                binding.etDoctorPhone.setText(selectedDoctor?.doc_phone ?: "")
                Toast.makeText(this, "Doctor selected: ${selectedDoctor?.doc_name}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun saveAppointment() {
        val doctorName = binding.etDoctorName.text.toString().trim()
        val doctorPhone = binding.etDoctorPhone.text.toString().trim()
        val appointmentDate = binding.etAppointmentDate.text.toString().trim()
        val appointmentTime = binding.etAppointmentTime.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        // Validation
        if (doctorName.isEmpty() || appointmentDate.isEmpty() || appointmentTime.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields (Doctor, Date, Time)", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDoctor == null) {
            Toast.makeText(this, "Please select a doctor", Toast.LENGTH_SHORT).show()
            return
        }

        // Get token from SharedPreferences
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare request body
        val appointmentRequest = BookAppointmentRequest(
            doctor_id = selectedDoctor!!.doc_id,
            appointment_date = appointmentDate,
            appointment_time = appointmentTime,
            notes = notes.ifEmpty { null }
        )

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Booking appointment: $appointmentRequest")
                
                val response = ApiClient.retrofit.bookAppointment("Bearer $token", appointmentRequest)
                
                Log.d(TAG, "✅ Appointment booked successfully: ${response.message}")
                Toast.makeText(
                    this@BookAppointmentActivity,
                    "Appointment booked successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                // Clear form
                clearForm()
                loadSavedAppointments()

            } catch (e: Exception) {
                Log.e(TAG, "Error booking appointment: ${e.message}", e)
                Toast.makeText(
                    this@BookAppointmentActivity,
                    "Failed to book appointment: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadSavedAppointments() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        if (token.isEmpty()) {
            Log.w(TAG, "No token available for loading appointments")
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.retrofit.getPatientAppointments("Bearer $token")
                
                // Convert API appointments to local entities for display
                val appointmentEntities = response.appointments.map { apt ->
                    AppointmentEntity(
                        appointment_id = apt.appointment_id,
                        patient_id = apt.patient_id,
                        doctor_name = "Doctor ID: ${apt.doctor_id}",
                        doctor_phone = "N/A",
                        doctor_type = "consultation",
                        appointment_date = apt.appointment_date,
                        appointment_time = apt.appointment_time,
                        notes = apt.notes,
                        synced = true,
                        sync_status = "synced"
                    )
                }

                appointments.clear()
                appointments.addAll(appointmentEntities)
                appointmentAdapter.notifyDataSetChanged()

                // Show/hide empty state
                if (appointments.isEmpty()) {
                    binding.emptyState.visibility = android.view.View.VISIBLE
                    binding.rvSavedAppointments.visibility = android.view.View.GONE
                } else {
                    binding.emptyState.visibility = android.view.View.GONE
                    binding.rvSavedAppointments.visibility = android.view.View.VISIBLE
                }
                
                Log.d(TAG, "✅ Loaded ${appointments.size} appointments")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading appointments: ${e.message}", e)
                Toast.makeText(this@BookAppointmentActivity, "Failed to load appointments", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteAppointment(appointment: AppointmentEntity) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Appointment")
            .setMessage("Are you sure you want to delete this appointment?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        // In production, you would call the API to delete from backend
                        // For now, just refresh the list
                        Toast.makeText(
                            this@BookAppointmentActivity,
                            "Appointment deletion not available",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadSavedAppointments()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@BookAppointmentActivity,
                            "Error deleting appointment",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearForm() {
        binding.etDoctorName.text.clear()
        binding.etDoctorPhone.text.clear()
        binding.etAppointmentDate.text.clear()
        binding.etAppointmentTime.text.clear()
        binding.etNotes.text.clear()
        selectedDoctor = null
    }

    private fun isValidDateFormat(date: String): Boolean {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format(
                Locale.getDefault(),
                "%04d-%02d-%02d",
                selectedYear,
                selectedMonth + 1,
                selectedDay
            )
            binding.etAppointmentDate.setText(formattedDate)
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                selectedHour,
                selectedMinute
            )
            binding.etAppointmentTime.setText(formattedTime)
        }, hour, minute, true).show()
    }
}
