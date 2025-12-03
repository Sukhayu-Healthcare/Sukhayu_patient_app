package com.sukhayu.patient.ui.asha.family

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.PatientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MemberListActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvFamilyTitle: TextView
    private lateinit var rvMembers: RecyclerView
    private lateinit var adapter: FamilyMemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_list)

        // Set up toolbar
        supportActionBar?.apply {
            title = "Family Members"
            setDisplayHomeAsUpEnabled(true)
        }

        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvFamilyTitle = findViewById(R.id.tvFamilyTitle)
        rvMembers = findViewById(R.id.rvMembers)

        adapter = FamilyMemberAdapter { patient ->
            openGeneralSurvey(patient)
        }

        rvMembers.layoutManager = LinearLayoutManager(this)
        rvMembers.adapter = adapter

        val supremeId = intent.getStringExtra("SUPREME_ID")
        if (supremeId != null) {
            loadMembers(supremeId)
        } else {
            finish()
        }
    }

    private fun loadMembers(supremeId: String) {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE

            val members = withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).patientDao()
                val allPatients = dao.getAllPatients()

                // Filter patients by supremeId or their own id equals supremeId
                allPatients.filter {
                    it.supremeId == supremeId || it.id == supremeId
                }.sortedBy { it.name }
            }

            progressBar.visibility = View.GONE

            if (members.isNotEmpty()) {
                // Extract family surname from first member
                val surname = members.first().name.trim().split(" ").lastOrNull() ?: "Unknown"
                tvFamilyTitle.text = "$surname Family Members (${members.size})"
            }

            adapter.submitList(members)
            tvEmpty.visibility = if (members.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openGeneralSurvey(patient: PatientEntity) {
        val intent = Intent(this, GeneralSurveyActivity::class.java).apply {
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_ID, patient.id)
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_NAME, patient.name)
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_PHONE, patient.phone ?: "")
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_GENDER, patient.gender ?: "")
            putExtra(GeneralSurveyActivity.EXTRA_PATIENT_AGE, patient.age?.toString() ?: "")
        }
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

