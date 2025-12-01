package com.sukhayu.patient.ui.supervisor

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.supervisor.drives.DriveAdapter
import com.sukhayu.patient.ui.supervisor.drives.DriveItem
import com.sukhayu.patient.ui.supervisor.surveys.SurveyAdapter
import com.sukhayu.patient.ui.supervisor.surveys.SurveyItem

class ViewSurveysAndDrivesActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var spinnerType: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var etSearchQuery: EditText
    private lateinit var btnSearch: Button
    private lateinit var recyclerView: RecyclerView

    private var allSurveys = mutableListOf<SurveyItem>()
    private var allDrives = mutableListOf<DriveItem>()
    private var currentTab = "Survey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_surveys_drives)

        initViews()
        setupTabs()
        setupSpinners()
        setupListeners()
        loadDummyData()
        displaySurveys()
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tabLayout)
        spinnerType = findViewById(R.id.spinnerType)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        etSearchQuery = findViewById(R.id.etSearchQuery)
        btnSearch = findViewById(R.id.btnSearch)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Surveys"))
        tabLayout.addTab(tabLayout.newTab().setText("Drives"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = if (tab?.position == 0) "Survey" else "Drive"
                etSearchQuery.text.clear()
                if (currentTab == "Survey") displaySurveys() else displayDrives()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSpinners() {
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            arrayOf("All Types", "Tuberculosis", "Pregnancy", "General Survey", "Health Camp"))
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            arrayOf("All Status", "Past", "Ongoing", "Upcoming"))
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter
    }

    private fun setupListeners() {
        btnSearch.setOnClickListener { performSearch() }
    }

    private fun loadDummyData() {
        allSurveys.addAll(listOf(
            SurveyItem("TB Screening Campaign", "2024-01-15", "Tuberculosis", "Completed", "Ramesh Kumar", 45),
            SurveyItem("Pregnancy ANC Program", "2024-01-20", "Pregnancy", "Ongoing", "Priya Singh", 28),
            SurveyItem("General Health Check", "2024-02-01", "General Survey", "Upcoming", "Amit Patel", 0),
            SurveyItem("TB Follow-up Drive", "2023-12-20", "Tuberculosis", "Completed", "Ramesh Kumar", 35)
        ))

        allDrives.addAll(listOf(
            DriveItem("Blood Camp Drive", "2024-01-18", "Community Center, Ward 5", "Successful blood collection camp", "Priya Singh"),
            DriveItem("Vaccination Drive", "2024-01-25", "Primary Health Center", "Child immunization program", "Amit Patel"),
            DriveItem("Health Awareness Camp", "2024-02-05", "Market Square, Main Bazaar", "General health awareness and registration", "Ramesh Kumar")
        ))
    }

    private fun displaySurveys() {
        val adapter = SurveyAdapter(allSurveys)
        recyclerView.adapter = adapter
    }

    private fun displayDrives() {
        val adapter = DriveAdapter(allDrives)
        recyclerView.adapter = adapter
    }

    private fun performSearch() {
        val type = spinnerType.selectedItem.toString()
        val status = spinnerStatus.selectedItem.toString()
        val query = etSearchQuery.text.toString().lowercase()

        if (currentTab == "Survey") {
            val filtered = allSurveys.filter { survey ->
                (type == "All Types" || survey.type == type) &&
                (status == "All Status" || survey.status == status) &&
                (query.isEmpty() || survey.surveyName.lowercase().contains(query) ||
                        survey.ashaName.lowercase().contains(query))
            }
            recyclerView.adapter = SurveyAdapter(filtered)
        } else {
            val filtered = allDrives.filter { drive ->
                (status == "All Status" || (if (status == "Completed") drive.driveName.contains("Blood") else true)) &&
                (query.isEmpty() || drive.driveName.lowercase().contains(query) ||
                        drive.ashaName.lowercase().contains(query) ||
                        drive.venue.lowercase().contains(query))
            }
            recyclerView.adapter = DriveAdapter(filtered)
        }
    }
}
