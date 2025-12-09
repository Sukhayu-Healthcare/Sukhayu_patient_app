package com.sukhayu.patient.ui.consultation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.Consultation
import com.sukhayu.patient.data.remote.PatientConsultationsResponse
import com.sukhayu.patient.data.repository.ConsultationRepository
import com.sukhayu.patient.utils.NetworkUtils
import com.sukhayu.utils.VoiceInputHelper
import com.sukhayu.patient.utils.formatDate
import kotlinx.coroutines.launch
import com.sukhayu.patient.utils.HeaderUtils
import java.io.File
import java.io.FileOutputStream


class PastConsultationsActivity : AppCompatActivity() {

    private lateinit var repository: ConsultationRepository
    private lateinit var voiceHelper: VoiceInputHelper
    private lateinit var llPast: LinearLayout
    private val TAG = "PastConsultationsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_consultations)
        HeaderUtils.setupRoleInHeader(this)
        
        llPast = findViewById(R.id.llPast)
        val db = AshaLocalDatabase.getInstance(this)
        repository = ConsultationRepository(db)

        // Fetch consultations from backend
        fetchConsultationsFromBackend()

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
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
                    
                    if (consultations.isEmpty()) {
                        showNoConsultations()
                    } else {
                        displayConsultations(consultations)
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

    private fun displayConsultations(consultations: List<Consultation>) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "📝 DISPLAYING ${consultations.size} CONSULTATIONS")
        Log.d(TAG, "========================================")

        consultations.forEach { c ->
            Log.d(TAG, "Consultation ID: ${c.consultation_id}, Doctor: ${c.doctor_name}, Date: ${c.consultation_date}, Items: ${c.items?.size ?: 0}")
            llPast.addView(createConsultationView(c))
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

            consultationsLocal.forEach { c ->
                val card = layoutInflater.inflate(R.layout.item_consultation_card, llPast, false)

                card.findViewById<TextView>(R.id.tvDoctor).text = "Doctor: ${c.doctor_id}"
                card.findViewById<TextView>(R.id.tvDate).text = formatDate(c.consultation_date)
                card.findViewById<TextView>(R.id.tvNotes).text = c.notes ?: "No Notes"

                llPast.addView(card)
            }
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
        voiceHelper.destroy()
    }
}
