package com.sukhayu.patient.ui.consultation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.DummyData
import com.sukhayu.patient.Doctor
import com.sukhayu.patient.DoctorAdapter
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.ai_symptom.CheckSymptomsActivity
import com.sukhayu.patient.ui.ai_symptom.SymptomRules

class ConsultDoctorActivity : AppCompatActivity() {

    private lateinit var rvDoctors: RecyclerView
    private lateinit var adapter: DoctorAdapter
    private lateinit var doctors: MutableList<Doctor>
    private lateinit var btnStartQuestionnaire: Button
    private lateinit var tvStatus: TextView

    // If user uses the Start Questionnaire button within this screen, we still support that.
    private val symptomLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val symptomText = result.data!!.getStringExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_TEXT) ?: ""
            val analysis = result.data!!.getStringExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_ANALYSIS) ?: ""
            onSymptomResult(symptomText, analysis)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consult_doctor)

        rvDoctors = findViewById(R.id.rvDoctors)
        btnStartQuestionnaire = findViewById(R.id.btnStartQuestionnaire)
        tvStatus = findViewById(R.id.tvStatus)

        rvDoctors.layoutManager = LinearLayoutManager(this)

        // load doctors from your DummyData
        doctors = DummyData.getDoctors().toMutableList()

        adapter = DoctorAdapter(doctors) { doctor ->
            // Launch DoctorDetailActivity (exists in same package)
            val intent = Intent(this, DoctorDetailActivity::class.java)
            intent.putExtra("doctor_name", doctor.name)
            intent.putExtra("doctor_specialty", doctor.specialty)
            intent.putExtra("doctor_rating", doctor.rating)
            startActivity(intent)
        }

        rvDoctors.adapter = adapter

        // Start questionnaire on button click (when user uses Start button inside Consult screen)
        btnStartQuestionnaire.setOnClickListener {
            val intent = Intent(this, CheckSymptomsActivity::class.java)
            symptomLauncher.launch(intent)
        }

        // Back button (if exists in layout)
        val btnBack = findViewById<View?>(R.id.btnBack)
        btnBack?.setOnClickListener { finish() }

        // --- NEW: If started with symptom data (from Dashboard -> questionnaire), auto-handle it ---
        val incomingSymptom = intent.getStringExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_TEXT)
        val incomingAnalysis = intent.getStringExtra(CheckSymptomsActivity.EXTRA_SYMPTOM_ANALYSIS)
        if (!incomingSymptom.isNullOrEmpty() || !incomingAnalysis.isNullOrEmpty()) {
            // hide Start button because we already have data
            btnStartQuestionnaire.visibility = View.GONE

            // auto allocate & show result
            onSymptomResult(incomingSymptom ?: "", incomingAnalysis ?: "")
        }
    }

    private fun onSymptomResult(symptomText: String, analysis: String) {
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = "Analysis: $analysis"

        val match = allocateDoctor(symptomText, analysis, doctors)

        if (match != null) {
            doctors.remove(match)
            doctors.add(0, match)
            adapter.notifyDataSetChanged()

            tvStatus.text = "Analysis: $analysis\nRecommended: ${match.name} (${match.specialty})"
        } else {
            tvStatus.text = "Analysis: $analysis\nNo exact match found. You can choose any doctor."
        }
    }

    private fun allocateDoctor(symptomText: String, analysis: String, list: List<Doctor>): Doctor? {
        val text = (symptomText + " " + analysis).lowercase()

        // mapping words to specialties (extend as required)
        val rules = listOf(
            "general" to listOf("fever", "viral", "infection", "tired", "weakness"),
            "pediatric" to listOf("child", "kids", "baby"),
            "derma" to listOf("skin", "rash", "itch", "allergy"),
        )

        // check rules first
        for ((key, keywords) in rules) {
            if (keywords.any { text.contains(it) }) {
                // find first doctor whose specialty contains a relevant substring
                return list.firstOrNull { it.specialty.lowercase().contains(key.substring(0, 4)) }
            }
        }

        // fallback: keyword search inside specialty
        for (doctor in list) {
            if (text.contains(doctor.specialty.lowercase().substring(0, 3))) {
                return doctor
            }
        }

        return null
    }
}
