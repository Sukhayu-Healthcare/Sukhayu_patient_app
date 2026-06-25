package com.sukhayu.patient.ui.supervisor

import android.app.DatePickerDialog
import android.content.ContentValues
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.SupervisorSurveyDataResponse
import com.sukhayu.patient.utils.HeaderUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
import android.widget.AdapterView
import com.sukhayu.patient.utils.TtsHelper
import com.sukhayu.patient.utils.ViewTtsHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.utils.LocalizableActivity
import com.sukhayu.utils.VoiceInputHelper

class ViewSurveysAndDrivesActivity : LocalizableActivity() {

    private lateinit var spinnerSurveyType: Spinner
    private lateinit var etDate: EditText
    private lateinit var btnFetchData: Button
    private lateinit var tvSummary: TextView
    private lateinit var btnDownload: Button
    private lateinit var recordContainer: LinearLayout
    private lateinit var tableLayout: TableLayout

    private lateinit var ttsHelper: TtsHelper

    private lateinit var voiceHelper: VoiceInputHelper

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

        HeaderUtils.setupRoleInHeader(this)
        initViews()
        setupSpinner()
        setupListeners()
        setupLanguageToggle()

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

        // Voice input setup
        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun initViews() {
        spinnerSurveyType = findViewById(R.id.spinnerSurveyType)
        etDate = findViewById(R.id.etDate)
        btnFetchData = findViewById(R.id.btnFetchData)
        tvSummary = findViewById(R.id.tvSummary)
        btnDownload = findViewById(R.id.btnDownload)
        recordContainer = findViewById(R.id.recordContainer)
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

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        if (token.isEmpty()) {
            Toast.makeText(this, "Authentication token not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                btnFetchData.isEnabled = false
                btnFetchData.text = "Loading..."

                val response = ApiClient.retrofit.getSupervisorSurveyDataByTableAndDate(
                    "Bearer $token",
                    tableName,
                    date
                )

                currentData = response
                updateSummary(response)
                displayCards(response)
                displayTable(response)

            } catch (e: Exception) {
                Toast.makeText(this@ViewSurveysAndDrivesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } finally {
                btnFetchData.isEnabled = true
                btnFetchData.text = "Fetch Survey Data"
            }
        }
    }

    private fun updateSummary(data: SupervisorSurveyDataResponse) {
        tvSummary.text = "Total Records: ${data.count} | ASHAs: ${data.asha_count}"
        btnDownload.isEnabled = data.count > 0
    }

    private fun displayCards(data: SupervisorSurveyDataResponse) {
        recordContainer.removeAllViews()
        if (data.records.isEmpty()) return

        // Group records by date
        val groupedByDate = data.records.groupBy { it["date"] ?: "Unknown Date" }

        groupedByDate.forEach { (date, recordsByDate) ->
            // Date header card
            val dateCard = CardView(this).apply {
                radius = 12f
                setCardBackgroundColor(Color.parseColor("#BBDEFB"))
                setContentPadding(16, 16, 16, 16)
            }
            val dateText = TextView(this).apply {
                text = "Date: $date"
                textSize = 16f
                setTextColor(Color.BLACK)
                setPadding(8, 8, 8, 8)
            }
            dateCard.addView(dateText)
            recordContainer.addView(dateCard)

            // ASHA-wise cards under each date
            val groupedByAsha = recordsByDate.groupBy { it["asha_name"] ?: "Unknown ASHA" }
            groupedByAsha.forEach { (ashaName, ashaRecords) ->
                val ashaCard = CardView(this).apply {
                    radius = 12f
                    setCardBackgroundColor(Color.parseColor("#E3F2FD"))
                    setContentPadding(16, 16, 16, 16)
                }
                val layout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val ashaHeader = TextView(this).apply {
                    text = "ASHA: $ashaName (ID: ${ashaRecords.firstOrNull()?.get("asha_id") ?: "N/A"})"
                    textSize = 14f
                    setTextColor(Color.BLACK)
                    setPadding(8, 8, 8, 8)
                }
                layout.addView(ashaHeader)

                // Show record details
                ashaRecords.forEach { record ->
                    record.forEach { (key, value) ->
                        if (key != "asha_name" && key != "asha_id" && key != "date") {
                            val tv = TextView(this).apply {
                                text = "${key.replace("_", " ").uppercase()}: ${value ?: "N/A"}"
                                setPadding(8, 4, 8, 4)
                            }
                            layout.addView(tv)
                        }
                    }
                }

                ashaCard.addView(layout)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8)
                }
                recordContainer.addView(ashaCard, lp)
            }
        }
    }

    private fun displayTable(data: SupervisorSurveyDataResponse) {
        tableLayout.removeAllViews()
        if (data.records.isEmpty()) return

        val headers = data.records.firstOrNull()?.keys?.toList() ?: return
        val headerRow = TableRow(this)
        headers.forEach { key ->
            val tv = TextView(this).apply {
                text = key.replace("_", " ").uppercase()
                setPadding(12, 12, 12, 12)
                setBackgroundColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                minWidth = 150
            }
            headerRow.addView(tv)
        }
        tableLayout.addView(headerRow)

        data.records.forEach { record ->
            val row = TableRow(this)
            headers.forEach { key ->
                val tv = TextView(this).apply {
                    text = record[key]?.toString() ?: "N/A"
                    setPadding(12, 12, 12, 12)
                    setBackgroundColor(Color.WHITE)
                    minWidth = 150
                }
                row.addView(tv)
            }
            tableLayout.addView(row)
        }
    }

    private fun exportToCsv() {
        val data = currentData ?: return
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "survey_${timestamp}.csv"

            val csvContent = buildString {
                val headers = data.records.firstOrNull()?.keys?.toList() ?: return@buildString
                appendLine(headers.joinToString(",") { "\"${it.replace("_", " ").uppercase()}\"" })
                data.records.forEach { record ->
                    val row = headers.joinToString(",") { key ->
                        "\"${record[key]?.toString()?.replace("\"", "\"\"") ?: "N/A"}\""
                    }
                    appendLine(row)
                }
            }

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
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SukhayuSurveys")
        }
        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { out -> out.write(content.toByteArray()) }
            Toast.makeText(this, "CSV saved: Downloads/SukhayuSurveys/$fileName", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveToDownloadsLegacy(fileName: String, content: String) {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SukhayuSurveys")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, fileName)
        file.writeText(content)
        Toast.makeText(this, "CSV saved: Downloads/SukhayuSurveys/$fileName", Toast.LENGTH_LONG).show()
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
}
