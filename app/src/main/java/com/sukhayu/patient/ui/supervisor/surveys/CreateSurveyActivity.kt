package com.sukhayu.patient.ui.supervisor.surveys

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.R
import com.sukhayu.utils.VoiceInputHelper
import java.text.SimpleDateFormat
import com.sukhayu.patient.utils.HeaderUtils
import java.util.*

class CreateSurveyActivity : AppCompatActivity() {

    private lateinit var spinnerSurveyType: Spinner
    private lateinit var btnSelectDate: Button
    private lateinit var tvSelectedDate: TextView
    private lateinit var etRemark: EditText
    private lateinit var btnMicRemark: ImageButton
    private lateinit var btnCreate: Button
    private lateinit var btnCancel: Button
    private lateinit var voiceHelper: VoiceInputHelper

    private var selectedDate: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_survey)
        HeaderUtils.setupRoleInHeader(this)
        initViews()
        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        setupSpinner()
        setupListeners()
    }

    private fun initViews() {
        spinnerSurveyType = findViewById(R.id.spinnerSurveyType)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        etRemark = findViewById(R.id.etRemark)
        btnMicRemark = findViewById(R.id.btnMicRemark)
        btnCreate = findViewById(R.id.btnCreate)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupSpinner() {
        val surveyTypes = arrayOf("Select Survey Type", "Tuberculosis", "Pregnancy", "General Survey")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, surveyTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSurveyType.adapter = adapter
    }

    private fun setupListeners() {
        btnSelectDate.setOnClickListener { showDatePicker() }
        btnMicRemark.setOnClickListener { startVoiceInput() }
        btnCreate.setOnClickListener { createSurvey() }
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

    private fun startVoiceInput() {
        voiceHelper.startVoiceInput(etRemark)
    }

    private fun createSurvey() {
        val surveyType = spinnerSurveyType.selectedItem.toString()
        val remark = etRemark.text.toString()

        if (surveyType == "Select Survey Type") {
            Toast.makeText(this, "Please select survey type", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Call API to create survey
        Toast.makeText(this, "Survey created: $surveyType on $selectedDate", Toast.LENGTH_SHORT).show()
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
