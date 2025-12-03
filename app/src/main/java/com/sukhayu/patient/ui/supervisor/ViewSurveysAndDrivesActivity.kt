package com.sukhayu.patient.ui.supervisor

import android.app.DatePickerDialog
import android.content.ContentValues
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.SupervisorSurveyDataResponse
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ViewSurveysAndDrivesActivity : AppCompatActivity() {

    private lateinit var spinnerSurveyType: Spinner
    private lateinit var etDate: EditText
    private lateinit var btnFetchData: Button
    private lateinit var tvSummary: TextView
    private lateinit var btnDownload: Button
    private lateinit var tableLayout: TableLayout

    private val surveyTypes = mapOf(
        "Patient Screening" to "patient_screening",
        "TB Patients" to "tb_patients",
        "TB Follow-ups" to "tb_followups",
        "ANC First Visit" to "anc_first_visit",
        "ANC Follow-up Visit" to "anc_followup_visit"
    )

    private var currentData: SupervisorSurveyDataResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_surveys_and_drives)

        initViews()
        setupSpinner()
        setupListeners()
    }

    private fun initViews() {
        spinnerSurveyType = findViewById(R.id.spinnerSurveyType)
        etDate = findViewById(R.id.etDate)
        btnFetchData = findViewById(R.id.btnFetchData)
        tvSummary = findViewById(R.id.tvSummary)
        btnDownload = findViewById(R.id.btnDownload)
        tableLayout = findViewById(R.id.tableLayout)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            surveyTypes.keys.toList()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSurveyType.adapter = adapter
    }

    private fun setupListeners() {
        etDate.setOnClickListener { showDatePicker() }
        btnFetchData.setOnClickListener { fetchSurveyData() }
        btnDownload.setOnClickListener { exportToCsv() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val date = String.format("%04d-%02d-%02d", year, month + 1, day)
                etDate.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun fetchSurveyData() {
        val selectedType = spinnerSurveyType.selectedItem.toString()
        val tableName = surveyTypes[selectedType] ?: return
        val date = etDate.text.toString()

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val token = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("token", "") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "Authentication token not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                btnFetchData.isEnabled = false
                btnFetchData.text = "Loading..."

                val response = ApiClient.retrofit.getSupervisorSurveyData(
                    "Bearer $token",
                    tableName,
                    date
                )

                currentData = response
                updateUI(response)
                displayTable(response)

            } catch (e: Exception) {
                Toast.makeText(
                    this@ViewSurveysAndDrivesActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                btnFetchData.isEnabled = true
                btnFetchData.text = "Fetch Survey Data"
            }
        }
    }

    private fun updateUI(data: SupervisorSurveyDataResponse) {
        tvSummary.text = "Total Records: ${data.count} | ASHAs: ${data.asha_count}"
        btnDownload.isEnabled = data.count > 0
    }

    private fun displayTable(data: SupervisorSurveyDataResponse) {
        tableLayout.removeAllViews()

        if (data.records.isEmpty()) {
            val noDataRow = TableRow(this)
            val noDataText = TextView(this).apply {
                text = "No records found"
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER
            }
            noDataRow.addView(noDataText)
            tableLayout.addView(noDataRow)
            return
        }

        // Header row
        val headerRow = TableRow(this)
        val headers = data.records.firstOrNull()?.keys?.toList() ?: return

        headers.forEach { header ->
            val headerText = TextView(this).apply {
                text = header.replace("_", " ").uppercase()
                setPadding(12, 12, 12, 12)
                setTypeface(null, Typeface.BOLD)
                setBackgroundColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                minWidth = 150
            }
            headerRow.addView(headerText)
        }
        tableLayout.addView(headerRow)

        // Data rows
        data.records.forEach { record ->
            val dataRow = TableRow(this)
            headers.forEach { key ->
                val cellText = TextView(this).apply {
                    text = record[key]?.toString() ?: "N/A"
                    setPadding(12, 12, 12, 12)
                    setBackgroundColor(Color.WHITE)
                    minWidth = 150
                }
                dataRow.addView(cellText)
            }
            tableLayout.addView(dataRow)

            // Divider between rows
            val divider = View(this).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    1
                )
                setBackgroundColor(Color.GRAY)
            }
            tableLayout.addView(divider)
        }
    }

    private fun exportToCsv() {
        val data = currentData ?: return

        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "survey_${data.table}_${data.date}_$timestamp.csv"

            // Build CSV content
            val csvContent = buildString {
                // Headers
                val headers = data.records.firstOrNull()?.keys?.toList() ?: return@buildString
                appendLine(headers.joinToString(",") { "\"${it.replace("_", " ").uppercase()}\"" })

                // Data rows
                data.records.forEach { record ->
                    val row = headers.joinToString(",") { key ->
                        val value = record[key]?.toString() ?: "N/A"
                        "\"${value.replace("\"", "\"\"")}\""
                    }
                    appendLine(row)
                }
            }

            // Save depending on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToDownloadsMediaStore(fileName, csvContent)
            } else {
                saveToDownloadsLegacy(fileName, csvContent)
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error exporting: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToDownloadsMediaStore(fileName: String, content: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/SukhayuSurveys"
            )
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
                outputStream.flush()
            }

            Toast.makeText(
                this,
                "CSV file saved to Downloads/SukhayuSurveys/$fileName",
                Toast.LENGTH_LONG
            ).show()
        } else {
            throw Exception("Failed to create file in Downloads")
        }
    }

    private fun saveToDownloadsLegacy(fileName: String, content: String) {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "SukhayuSurveys"
        )

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, fileName)
        file.writeText(content)

        Toast.makeText(
            this,
            "CSV file saved to Downloads/SukhayuSurveys/$fileName",
            Toast.LENGTH_LONG
        ).show()
    }
}
