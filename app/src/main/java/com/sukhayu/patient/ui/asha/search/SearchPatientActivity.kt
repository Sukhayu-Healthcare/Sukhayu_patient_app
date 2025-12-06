package com.sukhayu.patient.ui.asha.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.sukhayu.patient.R
import com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.ui.asha.family.FamilyListActivity
import com.sukhayu.utils.VoiceInputHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchPatientActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper
    private lateinit var etSearchQuery: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var rvPatients: RecyclerView
    private lateinit var adapter: PatientListAdapter
    private lateinit var toggleViewMode: MaterialButtonToggleGroup

    // remember current query so that after delete we reload with same filter
    private var currentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_search)

        etSearchQuery = findViewById(R.id.etSearchQuery)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        rvPatients = findViewById(R.id.rvPatients)
        toggleViewMode = findViewById(R.id.toggleViewMode)

        adapter = PatientListAdapter(
            onPatientSelected = { openGeneralSurvey(it) },
            onDeleteClick = { patient -> showDeleteConfirmation(patient) }
        )

        rvPatients.layoutManager = LinearLayoutManager(this)
        rvPatients.adapter = adapter

        setupSearchListener()
        setupToggleListener()

        // initial load with empty query = all patients
        currentQuery = ""
        loadPatients(currentQuery)

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun setupSearchListener() {
        etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentQuery = s?.toString()?.trim().orEmpty()
                loadPatients(currentQuery)
            }
        })
    }

    private fun setupToggleListener() {
        toggleViewMode.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                R.id.btnPatientsMode -> {
                    // Stay on this screen and reload patients
                    loadPatients(currentQuery)
                }

                R.id.btnFamiliesMode -> {
                    // Open the existing FamilyListActivity with the old family-wise logic
                    startActivity(Intent(this, FamilyListActivity::class.java))

                    // When user comes back here (back press), keep UI in Patients mode
                    group.check(R.id.btnPatientsMode)
                }
            }
        }
    }

    private fun requestAudioPermission() {
        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
        }
    }

    private fun loadPatients(query: String) {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE

            val patients = withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).patientDao()
                if (query.isBlank()) {
                    dao.getAllPatients()
                } else {
                    dao.searchPatients(query)
                }
            }

            progressBar.visibility = View.GONE
            adapter.submitList(patients)
            tvEmpty.visibility = if (patients.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showDeleteConfirmation(patient: PatientEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete patient")
            .setMessage("Do you want to delete ${patient.name ?: "this patient"} from this device?")
            .setPositiveButton("Delete") { _, _ ->
                deletePatient(patient)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePatient(patient: PatientEntity) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).patientDao()
                dao.deletePatientById(patient.id)
            }
            // reload list with same search query after deletion
            loadPatients(currentQuery)
        }
    }

    private fun openGeneralSurvey(patient: PatientEntity) {
        val intent = Intent(
            this,
            GeneralSurveyActivity::class.java
        ).apply {
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_ID, patient.id)
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_NAME, patient.name)
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_PHONE, patient.phone ?: "")
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_GENDER, patient.gender ?: "")
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_AGE, patient.age?.toString() ?: "")
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
