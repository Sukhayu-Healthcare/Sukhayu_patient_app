package com.sukhayu.patient.ui.supervisor.drives

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.utils.VoiceInputHelper
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.HeaderUtils
import java.text.SimpleDateFormat
import java.util.*

class CreateDriveActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper
    private lateinit var spinnerAnnouncementType: Spinner
    private lateinit var btnSelectDate: Button
    private lateinit var tvSelectedDate: TextView
    private lateinit var etVenue: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnMicMessage: ImageButton
    private lateinit var etRemark: EditText
    private lateinit var btnMicRemark: ImageButton
    private lateinit var btnCreate: Button
    private lateinit var btnCancel: Button

    private var selectedDate: String = ""
    private var selectedAnnouncementType: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val announcementTypes = arrayOf(
        "Select Type",
        "Training",
        "Monthly ASHA Meeting",
        "Immunization and Vaccination",
        "Audit"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_drive)
        HeaderUtils.setupRoleInHeader(this)
        initViews()
        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        setupAnnouncementTypeSpinner()
        setupListeners()
    }

    private fun initViews() {
        spinnerAnnouncementType = findViewById(R.id.spinnerAnnouncementType)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        etVenue = findViewById(R.id.etVenue)
        etMessage = findViewById(R.id.etMessage)
        btnMicMessage = findViewById(R.id.btnMicMessage)
        etRemark = findViewById(R.id.etRemark)
        btnMicRemark = findViewById(R.id.btnMicRemark)
        btnCreate = findViewById(R.id.btnCreate)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupAnnouncementTypeSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, announcementTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAnnouncementType.adapter = adapter

        spinnerAnnouncementType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedAnnouncementType = if (position > 0) announcementTypes[position] else ""
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedAnnouncementType = ""
            }
        }
    }

    private fun setupListeners() {
        btnSelectDate.setOnClickListener { showDatePicker() }
        btnMicMessage.setOnClickListener { startVoiceInput(etMessage) }
        btnMicRemark.setOnClickListener { startVoiceInput(etRemark) }
        btnCreate.setOnClickListener { createAnnouncement() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = Calendar.getInstance()
            date.set(selectedYear, selectedMonth, selectedDay)
            selectedDate = dateFormat.format(date.time)
            tvSelectedDate.text = "Date: $selectedDate"
        }, year, month, day)
        
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        
        datePickerDialog.show()
    }

    private fun startVoiceInput(editText: EditText) {
        voiceHelper.startVoiceInput(editText)
    }

    private fun createAnnouncement() {
        val venue = etVenue.text.toString().trim()
        val message = etMessage.text.toString().trim()
        val notes = etRemark.text.toString().trim()

        // Validation
        if (selectedAnnouncementType.isEmpty()) {
            Toast.makeText(this, "Please select announcement type", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select event date", Toast.LENGTH_SHORT).show()
            return
        }

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter announcement message", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Call API to create announcement and send to ASHA workers
        val venueText = if (venue.isNotEmpty()) "\nVenue: $venue" else ""
        val successMessage = "Announcement sent to ASHA workers:\nType: $selectedAnnouncementType\nDate: $selectedDate$venueText"
        Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}