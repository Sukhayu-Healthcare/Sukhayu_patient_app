package com.sukhayu.patient.ui.patient.appointment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.AppointmentEntity
import com.sukhayu.patient.databinding.ActivityBookAppointmentBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Local Doctor data class for appointment booking
data class DoctorAppointment(
    val name: String,
    val phone: String,
    val specialization: String,
    val availableDays: String
)

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookAppointmentBinding
    private lateinit var db: AshaLocalDatabase
    private var selectedDoctorType: String = "community_health_officer"
    private var selectedDoctor: DoctorAppointment? = null
    private val appointments = mutableListOf<AppointmentEntity>()
    private lateinit var appointmentAdapter: AppointmentAdapter

    // Sample doctors data (in production, fetch from API)
    private val choList = listOf(
        DoctorAppointment("Dr. Rajesh Kumar", "9876543210", "CHO", "Mon-Fri"),
        DoctorAppointment("Dr. Priya Singh", "9876543211", "CHO", "Tue-Sat"),
        DoctorAppointment("Dr. Amit Patel", "9876543212", "CHO", "Mon-Wed-Fri")
    )

    private val moList = listOf(
        DoctorAppointment("Dr. Vikram Sharma", "9876543220", "MO", "Mon-Fri"),
        DoctorAppointment("Dr. Neha Gupta", "9876543221", "MO", "Wed-Sat"),
        DoctorAppointment("Dr. Suresh Desai", "9876543222", "MO", "Tue-Thu-Sat")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AshaLocalDatabase.getInstance(this)

        // Setup RecyclerView
        appointmentAdapter = AppointmentAdapter(appointments) { appointment ->
            deleteAppointment(appointment)
        }
        binding.rvSavedAppointments.apply {
            layoutManager = LinearLayoutManager(this@BookAppointmentActivity)
            adapter = appointmentAdapter
        }

        // Doctor type selection
        binding.btnChoSelect.setOnClickListener {
            selectedDoctorType = "community_health_officer"
            updateDoctorTypeUI()
            showDoctorSelector(choList)
        }

        binding.btnMoSelect.setOnClickListener {
            selectedDoctorType = "medical_officer"
            updateDoctorTypeUI()
            showDoctorSelector(moList)
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

        // Set default doctor type
        updateDoctorTypeUI()
    }

    private fun updateDoctorTypeUI() {
        when (selectedDoctorType) {
            "community_health_officer" -> {
                binding.btnChoSelect.setBackgroundColor(getColor(R.color.color_success))
                binding.btnChoSelect.setTextColor(getColor(android.R.color.white))
                binding.btnMoSelect.setBackgroundColor(getColor(android.R.color.white))
                binding.btnMoSelect.setTextColor(getColor(R.color.color_text_muted))
            }
            "medical_officer" -> {
                binding.btnChoSelect.setBackgroundColor(getColor(android.R.color.white))
                binding.btnChoSelect.setTextColor(getColor(R.color.color_text_muted))
                binding.btnMoSelect.setBackgroundColor(getColor(R.color.color_success))
                binding.btnMoSelect.setTextColor(getColor(android.R.color.white))
            }
        }
    }

    private fun showDoctorSelector(doctors: List<DoctorAppointment>) {
        val doctorNames = doctors.map { "${it.name} (${it.phone})" }.toTypedArray()
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Select Doctor")
            .setItems(doctorNames) { _, which ->
                selectedDoctor = doctors[which]
                binding.etDoctorName.setText(selectedDoctor?.name ?: "")
                binding.etDoctorPhone.setText(selectedDoctor?.phone ?: "")
                Toast.makeText(this, "Doctor selected", Toast.LENGTH_SHORT).show()
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
        if (doctorName.isEmpty() || doctorPhone.isEmpty() || appointmentDate.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get patient ID from SharedPreferences (stored as String, convert to Int)
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userIdStr = prefs.getString("user_id", "") ?: ""
        val patientId = userIdStr.toIntOrNull() ?: 0

        if (patientId == 0) {
            Toast.makeText(this, "Patient ID not found. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create appointment entity
        val appointment = AppointmentEntity(
            patient_id = patientId,
            doctor_name = doctorName,
            doctor_phone = doctorPhone,
            doctor_type = selectedDoctorType,
            appointment_date = appointmentDate,
            appointment_time = appointmentTime.ifEmpty { null },
            notes = notes.ifEmpty { null },
            synced = false,
            sync_status = "pending"
        )

        // Save to database
        lifecycleScope.launch {
            try {
                val appointmentId = db.appointmentDao().insertAppointment(appointment)
                
                // Update UI
                loadSavedAppointments()
                clearForm()

                Toast.makeText(
                    this@BookAppointmentActivity,
                    "Appointment saved successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Try to sync with backend (optional)
                syncAppointmentWithBackend(appointment.copy(appointment_id = appointmentId.toInt()))
            } catch (e: Exception) {
                Toast.makeText(
                    this@BookAppointmentActivity,
                    "Error saving appointment: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadSavedAppointments() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userIdStr = prefs.getString("user_id", "") ?: ""
        val patientId = userIdStr.toIntOrNull() ?: 0

        if (patientId == 0) return

        lifecycleScope.launch {
            db.appointmentDao().getPatientAppointments(patientId).collect { appointmentList ->
                appointments.clear()
                appointments.addAll(appointmentList)
                appointmentAdapter.notifyDataSetChanged()

                // Show/hide empty state
                if (appointments.isEmpty()) {
                    binding.emptyState.visibility = android.view.View.VISIBLE
                    binding.rvSavedAppointments.visibility = android.view.View.GONE
                } else {
                    binding.emptyState.visibility = android.view.View.GONE
                    binding.rvSavedAppointments.visibility = android.view.View.VISIBLE
                }
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
                        db.appointmentDao().deleteAppointment(appointment)
                        loadSavedAppointments()
                        Toast.makeText(
                            this@BookAppointmentActivity,
                            "Appointment deleted",
                            Toast.LENGTH_SHORT
                        ).show()
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

    private fun syncAppointmentWithBackend(appointment: AppointmentEntity) {
        // This will sync with the backend database
        // For now, we'll just mark it as synced after a delay
        lifecycleScope.launch {
            try {
                // TODO: Make API call to sync with backend
                // val response = ApiClient.retrofit.syncAppointment(appointment)
                
                // Mark as synced
                db.appointmentDao().markAsSynced(appointment.appointment_id)
                loadSavedAppointments()
            } catch (e: Exception) {
                // Mark sync as failed
                db.appointmentDao().markAsSyncFailed(appointment.appointment_id)
                loadSavedAppointments()
            }
        }
    }
}
