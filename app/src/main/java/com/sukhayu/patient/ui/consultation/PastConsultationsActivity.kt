package com.sukhayu.patient.ui.consultation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.Consultation
import com.sukhayu.patient.data.remote.PatientConsultationsResponse
import com.sukhayu.patient.data.repository.ConsultationRepository
import com.sukhayu.patient.utils.LocalizableActivity
import com.sukhayu.patient.utils.NetworkUtils
import com.sukhayu.utils.VoiceInputHelper
import com.sukhayu.patient.utils.formatDate
import kotlinx.coroutines.launch
import com.sukhayu.patient.utils.HeaderUtils
import java.io.File
import android.view.View
import android.widget.AdapterView
import com.sukhayu.patient.utils.TtsHelper
import com.sukhayu.patient.utils.ViewTtsHelper
import java.io.FileOutputStream


class PastConsultationsActivity : LocalizableActivity() {

    private lateinit var repository: ConsultationRepository
    private lateinit var voiceHelper: VoiceInputHelper

    private lateinit var ttsHelper: TtsHelper
    private lateinit var llPast: LinearLayout
    private lateinit var searchEditText: EditText
    private val TAG = "PastConsultationsActivity"
    private var allConsultations: List<Consultation> = emptyList()
    private var displayedConsultations: List<Consultation> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_consultations)
        HeaderUtils.setupRoleInHeader(this)

        // Initialize TTS
        ttsHelper = TtsHelper(this)

        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val currentLang = prefs.getString("My_Lang", "en") ?: "en"

        ttsHelper.setLanguage(currentLang)

        // Setup language toggle in header
        setupLanguageToggle()
        
        llPast = findViewById(R.id.llPast)
        searchEditText = findViewById(R.id.etSearch)
        
        val db = AshaLocalDatabase.getInstance(this)
        repository = ConsultationRepository(db)

        // Setup search listener
        setupSearchListener()

        // Fetch consultations from backend
        fetchConsultationsFromBackend()

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)

        // Enable TTS on all TextViews and Buttons
        ViewTtsHelper.attachToAllTextViews(
            findViewById(android.R.id.content),
            ttsHelper
        )
    }

    private fun setupSearchListener() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isEmpty()) {
                    displayedConsultations = allConsultations
                } else {
                    displayedConsultations = searchConsultations(query)
                }
                refreshConsultationDisplay()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchConsultations(query: String): List<Consultation> {
        val lowerQuery = query.lowercase()
        return allConsultations.filter { consultation ->
            consultation.doctor_name?.lowercase()?.contains(lowerQuery) == true ||
            consultation.diagnosis?.lowercase()?.contains(lowerQuery) == true ||
            consultation.notes?.lowercase()?.contains(lowerQuery) == true ||
            consultation.items?.any { 
                it.medicine_name?.lowercase()?.contains(lowerQuery) == true ||
                it.dosage?.lowercase()?.contains(lowerQuery) == true
            } == true
        }
    }

    private fun refreshConsultationDisplay() {
        llPast.removeAllViews()
        
        if (displayedConsultations.isEmpty()) {
            showNoConsultations()
        } else {
            Log.d(TAG, "📝 DISPLAYING ${displayedConsultations.size} CONSULTATIONS")
            displayedConsultations.forEach { c ->
                llPast.addView(createConsultationView(c))
            }
        }
    }

    private fun fetchConsultationsFromBackend() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        if (token.isEmpty()) {
            Log.e(TAG, "No auth token found")
            showError("Session expired. Please login again.")
            return
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "📥 FETCHING PATIENT CONSULTATIONS")
        Log.d(TAG, "Auth Token: $token")
        Log.d(TAG, "Endpoint: /patient/consultations")
        Log.d(TAG, "========================================")

        lifecycleScope.launch {
            try {
                val response = ApiClient.retrofit.getPatientConsultations("Bearer $token")
                
                Log.d(TAG, "========================================")
                Log.d(TAG, "📡 BACKEND RESPONSE RECEIVED")
                Log.d(TAG, "Response: $response")
                Log.d(TAG, "========================================")

                if (response != null) {
                    Log.d(TAG, "✅ CONSULTATIONS FETCHED SUCCESSFULLY")
                    val consultations = response.consultations ?: emptyList()
                    Log.d(TAG, "Total Consultations: ${consultations.size}")
                    
                    allConsultations = consultations
                    displayedConsultations = consultations
                    
                    if (consultations.isEmpty()) {
                        showNoConsultations()
                    } else {
                        refreshConsultationDisplay()
                    }
                } else {
                    Log.e(TAG, "❌ Failed to fetch consultations. Response is null")
                    showError("Failed to load consultations")
                    showFromLocal()
                }
            } catch (e: Exception) {
                Log.e(TAG, "========================================")
                Log.e(TAG, "❌ NETWORK ERROR FETCHING CONSULTATIONS")
                Log.e(TAG, "Error Message: ${e.message}")
                Log.e(TAG, "Error Type: ${e.javaClass.simpleName}")
                Log.e(TAG, "========================================")
                e.printStackTrace()

                showError("Network error: ${e.message}")
                showFromLocal()
            }
        }
    }

    private fun showFromLocal() {
        Log.d(TAG, "Loading consultations from local database...")
        lifecycleScope.launch {
            val hasNetwork = NetworkUtils.isNetworkAvailable(this@PastConsultationsActivity)
            val consultationsLocal = repository.getLatestConsultations(hasNetwork)

            Log.d(TAG, "Local consultations found: ${consultationsLocal.size}")

            if (consultationsLocal.isEmpty()) {
                showNoConsultations()
                return@launch
            }

            allConsultations = consultationsLocal.mapNotNull { localConsult ->
                Consultation(
                    consultation_id = localConsult.consultation_id ?: 0,
                    patient_id = localConsult.patient_id ?: 0,
                    doctor_id = localConsult.doctor_id ?: 0,
                    doctor_name = "Doctor #${localConsult.doctor_id}",
                    doctor_phone = null,
                    diagnosis = null,
                    notes = localConsult.notes,
                    consultation_date = localConsult.consultation_date?.toString() ?: "",
                    items = emptyList()
                )
            }
            
            displayedConsultations = allConsultations
            refreshConsultationDisplay()
        }
    }

    private fun showNoConsultations() {
        val tv = TextView(this)
        tv.text = "No consultations found"
        tv.textSize = 16f
        tv.setPadding(16, 16, 16, 16)
        llPast.addView(tv)
    }

    private fun showError(message: String) {
        val tv = TextView(this)
        tv.text = "⚠️ $message"
        tv.textSize = 14f
        tv.setTextColor(android.graphics.Color.RED)
        tv.setPadding(16, 16, 16, 16)
        llPast.addView(tv)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun createConsultationView(c: Consultation): View {
        val root = layoutInflater.inflate(R.layout.item_consultation_card, llPast, false)

        val tvDoctor = root.findViewById<TextView>(R.id.tvDoctor)
        val tvDate = root.findViewById<TextView>(R.id.tvDate)
        val tvNotes = root.findViewById<TextView>(R.id.tvNotes)

        tvDoctor.text = "👨‍⚕️ Doctor: ${c.doctor_name ?: "Unknown"}"
        tvDate.text = "📅 Date: ${formatDateFlexible(c.consultation_date)}"
        tvNotes.text = "🔍 Diagnosis: ${c.diagnosis ?: "-"}\n📝 Notes: ${c.notes ?: "-"}"

        // Add a container below for items
        val itemsContainer = LinearLayout(this)
        itemsContainer.orientation = LinearLayout.VERTICAL
        itemsContainer.setPadding(12, 8, 12, 8)

        if (!c.items.isNullOrEmpty()) {
            val header = TextView(this)
            header.text = "💊 Prescription Items:"
            header.textSize = 14f
            header.setPadding(0, 6, 0, 6)
            itemsContainer.addView(header)

            c.items.forEach { item ->
                val tv = TextView(this)
                tv.text = "• ${item.medicine_name ?: "-"} | ${item.dosage ?: "-"} | ${item.frequency ?: "-"} | ${item.duration ?: "-"}"
                tv.textSize = 12f
                tv.setPadding(8, 4, 8, 4)
                itemsContainer.addView(tv)
            }
        }

        (root as LinearLayout).addView(itemsContainer)

        // Add Download PDF Button
        val btn = Button(this)
        btn.text = "📥 Download PDF"
        btn.setOnClickListener {
            requestStoragePermissionAndSavePdf(c)
        }

        root.addView(btn)

        return root
    }

    private fun formatDateFlexible(dateStr: String?): String {
        if (dateStr == null) return "Unknown Date"
        return try {
            val ts = dateStr.toLong()
            formatDate(ts)
        } catch (e: Exception) {
            try {
                if (dateStr.length >= 10) return dateStr.substring(0, 10)
            } catch (_: Exception) { }
            dateStr
        }
    }

    private fun requestStoragePermissionAndSavePdf(c: Consultation) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 201)
        } else {
            saveConsultationAsPdf(c)
        }
    }

    private fun saveConsultationAsPdf(c: Consultation) {
        try {
            val doc = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1
            var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            var canvas: Canvas = page.canvas
            val paint = Paint()
            var y = 40

            fun startNewPage() {
                doc.finishPage(page)
                pageNumber += 1
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = 40
            }

            paint.textSize = 16f
            canvas.drawText("Consultation ID: ${c.consultation_id}", 20f, y.toFloat(), paint)
            y += 24
            paint.textSize = 14f
            canvas.drawText("Date: ${formatDateFlexible(c.consultation_date)}", 20f, y.toFloat(), paint)
            y += 22
            canvas.drawText("Doctor: ${c.doctor_name ?: "-"} (${c.doctor_phone ?: "-"})", 20f, y.toFloat(), paint)
            y += 22
            canvas.drawText("Diagnosis: ${c.diagnosis ?: "-"}", 20f, y.toFloat(), paint)
            y += 22
            canvas.drawText("Notes: ${c.notes ?: "-"}", 20f, y.toFloat(), paint)
            y += 26

            paint.textSize = 15f
            canvas.drawText("Prescription:", 20f, y.toFloat(), paint)
            y += 20
            paint.textSize = 12f

            c.items?.forEach { item ->
                val line = "- ${item.medicine_name ?: "-"} | ${item.dosage ?: "-"} | ${item.frequency ?: "-"} | ${item.duration ?: "-"}"
                canvas.drawText(line, 22f, y.toFloat(), paint)
                y += 18
                if (y > pageHeight - 40) {
                    startNewPage()
                }
            }

            doc.finishPage(page)

            val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (dir != null && !dir.exists()) dir.mkdirs()
            val file = File(dir, "consultation_${c.consultation_id}.pdf")
            val fos = FileOutputStream(file)
            doc.writeTo(fos)
            fos.close()
            doc.close()

            Log.d(TAG, "PDF saved: ${file.absolutePath}")
            Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error saving PDF: ${e.message}", e)
            Toast.makeText(this, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()
        voiceHelper.destroy()
    }
}
