package com.sukhayu.patient.asha.ui.surveys

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R

class AshaViewSurveysActivity : AppCompatActivity() {

    private lateinit var rvSurveys: RecyclerView
    private lateinit var tvSyncSummary: TextView
    private lateinit var searchPatients: SearchView
    private lateinit var spinnerSurveyType: Spinner
    private lateinit var adapter: SurveySummaryAdapter
    private lateinit var viewModel: AshaViewSurveysViewModel

    // Keep full list for filtering
    private var fullSurveyList = listOf<SurveySummaryUiModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_view_surveys)

        // --- View references (matching your XML ids) ---
        rvSurveys = findViewById(R.id.rv_surveys)
        tvSyncSummary = findViewById(R.id.tv_sync_summary)
        searchPatients = findViewById(R.id.search_patients)
        spinnerSurveyType = findViewById(R.id.spinner_survey_type)

        // --- Adapter (onItemClick is optional for now) ---
        adapter = SurveySummaryAdapter { summary ->
            // TODO: handle click if needed
        }

        // --- RecyclerView ---
        rvSurveys.layoutManager = LinearLayoutManager(this)
        rvSurveys.adapter = adapter

        // --- ViewModel ---
        viewModel = ViewModelProvider(this)[AshaViewSurveysViewModel::class.java]

        // --- Setup SearchView ---
        setupSearchView()

        // --- Setup Spinner ---
        setupSpinner()

        // --- Observers ---
        viewModel.surveys.observe(this) { list ->
            fullSurveyList = list
            applyFilters()
        }

        viewModel.syncSummaryText.observe(this) { text ->
            tvSyncSummary.text = text
        }
    }

    /**
     * Setup SearchView to filter by patient name or survey type.
     */
    private fun setupSearchView() {
        searchPatients.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilters()
                return true
            }
        })
    }

    /**
     * Setup Spinner for filtering by survey type.
     */
    private fun setupSpinner() {
        val surveyTypes = listOf(
            "All Types",
            "TB Screening",
            "TB Follow-up",
            "ANC First Visit",
            "ANC Follow-up",
            "General Survey"
        )

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            surveyTypes
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSurveyType.adapter = spinnerAdapter

        spinnerSurveyType.setOnItemSelectedListener(
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    applyFilters()
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    // No action needed
                }
            }
        )
    }

    /**
     * Apply both SearchView and Spinner filters to the full list.
     */
    private fun applyFilters() {
        val searchQuery = searchPatients.query.toString().trim().lowercase()
        val selectedType = spinnerSurveyType.selectedItem?.toString() ?: "All Types"

        val filteredList = fullSurveyList.filter { survey ->
            val matchesSearch = searchQuery.isEmpty() ||
                    survey.patientName.lowercase().contains(searchQuery) ||
                    survey.surveyType.lowercase().contains(searchQuery)

            val matchesType = selectedType == "All Types" ||
                    survey.surveyType == selectedType

            matchesSearch && matchesType
        }

        adapter.submitList(filteredList)
    }
}
