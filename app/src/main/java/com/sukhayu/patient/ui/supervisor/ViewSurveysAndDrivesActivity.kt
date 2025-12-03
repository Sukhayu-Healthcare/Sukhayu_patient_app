package com.sukhayu.patient.ui.supervisor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R

/**
 * TEMPORARY placeholder for supervisor survey/drives view.
 *
 * The original implementation used models like SupervisorSurveyDataResponse,
 * records[], etc. that are missing after merge. To keep the project compiling,
 * we show a simple placeholder screen instead.
 *
 * Later, this can be rebuilt properly without blocking patient / ASHA flows.
 */
class ViewSurveysAndDrivesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_surveys_placeholder)

        supportActionBar?.apply {
            title = "Surveys & Drives"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
