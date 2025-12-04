package com.sukhayu.patient.ui.consultation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.Consultation
import com.sukhayu.patient.data.repository.ConsultationRepository
import com.sukhayu.patient.utils.NetworkUtils
import com.sukhayu.utils.VoiceInputHelper
import com.sukhayu.patient.utils.formatDate
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class PastConsultationsActivity : AppCompatActivity() {

    private lateinit var repository: ConsultationRepository
    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_consultations)

        val ll = findViewById<LinearLayout>(R.id.llPast)

        val db = AshaLocalDatabase.getInstance(this)
        repository = ConsultationRepository(db)

        lifecycleScope.launch {

            val hasNetwork = NetworkUtils.isNetworkAvailable(this@PastConsultationsActivity)

            if (hasNetwork) {
                // Try fetch from API
                try {
                    val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                    val token = prefs.getString("token", null)
                    if (token != null) {
                        val api = ApiClient.retrofit
                        val resp = api.getPatientConsultations("Bearer $token")
                        val consultations = resp.consultations

                        if (consultations.isEmpty()) {
                            val tv = TextView(this@PastConsultationsActivity)
                            tv.text = "No consultations found"
                            ll.addView(tv)
                        } else {
                            consultations.forEach { c ->
                                ll.addView(createConsultationView(c, ll))
                            }
                        }
                    } else {
                        // fallback to local DB
                        showFromLocal(ll)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    // fallback to local DB
                    showFromLocal(ll)
                }
            } else {
                // offline: show from local DB
                showFromLocal(ll)
            }
        }

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun showFromLocal(container: LinearLayout) {
        lifecycleScope.launch {
            val hasNetwork = NetworkUtils.isNetworkAvailable(this@PastConsultationsActivity)
            val consultationsLocal = repository.getLatestConsultations(hasNetwork)

            if (consultationsLocal.isEmpty()) {
                val tv = TextView(this@PastConsultationsActivity)
                tv.text = "No consultations found"
                container.addView(tv)
                return@launch
            }

            consultationsLocal.forEach { c ->
                val card = layoutInflater.inflate(R.layout.item_consultation_card, container, false)

                card.findViewById<TextView>(R.id.tvDoctor).text = "Doctor: ${c.doctor_id}"
                card.findViewById<TextView>(R.id.tvDate).text = formatDate(c.consultation_date)
                card.findViewById<TextView>(R.id.tvNotes).text = c.notes ?: "No Notes"

                container.addView(card)
            }
        }
    }

    // supply parent so inflate can resolve layout params
    private fun createConsultationView(c: Consultation, parent: LinearLayout): View {
        val root = layoutInflater.inflate(R.layout.item_consultation_card, parent, false)

        val tvDoctor = root.findViewById<TextView>(R.id.tvDoctor)
        val tvDate = root.findViewById<TextView>(R.id.tvDate)
        val tvNotes = root.findViewById<TextView>(R.id.tvNotes)

        tvDoctor.text = "Doctor: ${c.doctor_name ?: c.doctor_id}"
        tvDate.text = formatDateFlexible(c.consultation_date)
        tvNotes.text = "Diagnosis: ${c.diagnosis ?: "-"}\nNotes: ${c.notes ?: "-"}"

        // Add a container below for items
        val itemsContainer = LinearLayout(this)
        itemsContainer.orientation = LinearLayout.VERTICAL
        itemsContainer.setPadding(12, 8, 12, 8)

        val header = TextView(this)
        header.text = "Prescription Items:"
        header.setPadding(0, 6, 0, 6)
        itemsContainer.addView(header)

        c.items?.forEach { item ->
            val tv = TextView(this)
            tv.text = "• ${item.medicine_name ?: "-"} | ${item.dosage ?: "-"} | ${item.frequency ?: "-"} | ${item.duration ?: "-"} | ${item.instructions ?: "-"}"
            itemsContainer.addView(tv)
        }

        (root as LinearLayout).addView(itemsContainer)

        // Add Download PDF Button
        val btn = Button(this)
        btn.text = "Download PDF"
        btn.setOnClickListener {
            requestStoragePermissionAndSavePdf(c)
        }

        root.addView(btn)

        return root
    }

    private fun formatDateFlexible(dateStr: String?): String {
        if (dateStr == null) return "Unknown Date"
        // try numeric timestamp
        return try {
            val ts = dateStr.toLong()
            formatDate(ts)
        } catch (e: Exception) {
            // fallback to first 19 chars (ISO) or whole string
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
                // wrap manually if too long (naive)
                canvas.drawText(line, 22f, y.toFloat(), paint)
                y += 18
                if (y > pageHeight - 40) {
                    startNewPage()
                }
            }

            // finish last page
            doc.finishPage(page)

            val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (dir != null && !dir.exists()) dir.mkdirs()
            val file = File(dir, "consultation_${c.consultation_id}.pdf")
            val fos = FileOutputStream(file)
            doc.writeTo(fos)
            fos.close()
            doc.close()

            // simple feedback
            val tv = TextView(this)
            tv.text = "Saved PDF: ${file.absolutePath}"
            tv.setPadding(12, 10, 12, 10)
            findViewById<LinearLayout>(R.id.llPast).addView(tv)

        } catch (e: Exception) {
            e.printStackTrace()
            val tv = TextView(this)
            tv.text = "Failed to save PDF: ${e.message}"
            findViewById<LinearLayout>(R.id.llPast).addView(tv)
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
