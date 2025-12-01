package com.sukhayu.patient.asha.ui.surveys

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.sukhayu.patient.R
import com.sukhayu.patient.asha.ui.surveys.child.ChildSurveyActivity
import com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity
import com.sukhayu.patient.asha.ui.surveys.pregnancy.PregnancySurveyActivity
import com.sukhayu.patient.asha.ui.surveys.tb.TbSurveyActivity
import com.sukhayu.patient.ui.asha.search.PatientSearchForGeneralSurveyActivity
import com.sukhayu.utils.VoiceInputHelper

class AshaSurveyHomeActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AshaSurveyHome", "Activity onCreate called")
        setContentView(R.layout.activity_asha_survey_home)


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

        findViewById<MaterialCardView>(R.id.btnGeneralSurvey).setOnClickListener {
            // Navigate to patient search first, then to General Survey form
            startActivity(Intent(this, PatientSearchForGeneralSurveyActivity::class.java))
        }

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

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
