package com.sukhayu.patient.ui.asha.search

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.utils.VoiceInputHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.text.Editable
import android.text.TextWatcher

class SearchPatientActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper
    private lateinit var etSearchQuery: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var rvPatients: RecyclerView
    private lateinit var adapter: PatientListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_search)

        etSearchQuery = findViewById(R.id.etSearchQuery)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        rvPatients = findViewById(R.id.rvPatients)
        adapter = PatientListAdapter { openGeneralSurvey(it) }
        rvPatients.layoutManager = LinearLayoutManager(this)
        rvPatients.adapter = adapter

        etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim().orEmpty()
                loadPatients(query)
            }
        })

        loadPatients("")

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    private fun loadPatients(query: String) {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            val patients = withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).patientDao()
                if (query.isBlank()) dao.getAllPatients() else dao.searchPatients(query)
            }
            progressBar.visibility = View.GONE
            adapter.submitList(patients)
            tvEmpty.visibility = if (patients.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openGeneralSurvey(patient: PatientEntity) {
        val intent = android.content.Intent(this, com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity::class.java).apply {
            putExtra(com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity.EXTRA_PATIENT_ID, patient.id)
            putExtra(com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity.EXTRA_PATIENT_NAME, patient.name)
            putExtra(com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity.EXTRA_PATIENT_PHONE, patient.phone ?: "")
            putExtra(com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity.EXTRA_PATIENT_GENDER, patient.gender ?: "")
            putExtra(com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity.EXTRA_PATIENT_AGE, patient.age?.toString() ?: "")
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}