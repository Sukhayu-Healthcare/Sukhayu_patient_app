package com.sukhayu.patient.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.HeaderUtils
import com.sukhayu.patient.utils.TtsHelper

class DashboardActivity : AppCompatActivity() {

    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        HeaderUtils.setupRoleInHeader(this)
        ttsHelper = TtsHelper(this)
        ttsHelper.setLanguage("en")

        // TODO: Add TTS to your views
        // setupTtsForViews()
    }

    override fun onDestroy() {
        ttsHelper.shutdown()
        super.onDestroy()
    }
}