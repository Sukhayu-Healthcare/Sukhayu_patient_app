package com.sukhayu.patient.asha.ui.surveys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.sukhayu.patient.R
import com.sukhayu.patient.asha.ui.surveys.pregnancy.PregnancySurveyActivity
import com.sukhayu.patient.asha.ui.surveys.child.ChildSurveyActivity
import com.sukhayu.patient.asha.ui.surveys.tb.TbSurveyActivity
import com.sukhayu.patient.asha.ui.surveys.ncd.NcdSurveyActivity // Fixed import

class AshaSurveyHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AshaSurveyHome", "Activity onCreate called")
        setContentView(R.layout.activity_asha_survey_home)

        // Set toolbar title
        supportActionBar?.apply {
            title = "Surveys"
            setDisplayHomeAsUpEnabled(true)
        }

        // Wire survey button clicks
        findViewById<MaterialCardView>(R.id.btnPregnancySurvey).setOnClickListener {
            startActivity(Intent(this, PregnancySurveyActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.btnChildSurvey).setOnClickListener {
            startActivity(Intent(this, ChildSurveyActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.btnTbSurvey).setOnClickListener {
            startActivity(Intent(this, TbSurveyActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.btnNcdSurvey).setOnClickListener {
            startActivity(Intent(this, NcdSurveyActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
