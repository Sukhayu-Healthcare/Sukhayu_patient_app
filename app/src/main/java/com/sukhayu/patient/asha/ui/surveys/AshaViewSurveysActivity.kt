package com.sukhayu.patient.asha.ui.surveys

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R

class AshaViewSurveysActivity : AppCompatActivity() {

    private lateinit var rvSurveys: RecyclerView
    private lateinit var tvSyncSummary: TextView
    private lateinit var adapter: SurveySummaryAdapter
    private lateinit var viewModel: AshaViewSurveysViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_view_surveys)

        // --- View references (matching your XML ids) ---
        rvSurveys = findViewById(R.id.rv_surveys)
        tvSyncSummary = findViewById(R.id.tv_sync_summary)

        // --- Adapter (onItemClick is optional for now) ---
        adapter = SurveySummaryAdapter { summary ->
            // TODO: handle click if needed
        }

        // --- RecyclerView ---
        rvSurveys.layoutManager = LinearLayoutManager(this)
        rvSurveys.adapter = adapter

        // --- ViewModel ---
        viewModel = ViewModelProvider(this)[AshaViewSurveysViewModel::class.java]

        // --- Observers ---
        viewModel.surveys.observe(this) { list ->
            adapter.submitList(list)
        }

        viewModel.syncSummaryText.observe(this) { text ->
            tvSyncSummary.text = text
        }
    }
}
