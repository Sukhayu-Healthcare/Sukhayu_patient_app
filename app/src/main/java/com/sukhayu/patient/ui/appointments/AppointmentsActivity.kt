package com.sukhayu.patient.ui.appointments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.TtsHelper
import com.sukhayu.patient.utils.HeaderUtils
import com.sukhayu.patient.utils.LocalizableActivity

class AppointmentsActivity : LocalizableActivity() {

    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments)
        setupLanguageToggle()
        HeaderUtils.setupRoleInHeader(this)
        ttsHelper = TtsHelper(this)
        ttsHelper.setLanguage("en")

        // TODO: Add TTS to appointment list and details
    }

    override fun onDestroy() {
        ttsHelper.shutdown()
        super.onDestroy()
    }
}