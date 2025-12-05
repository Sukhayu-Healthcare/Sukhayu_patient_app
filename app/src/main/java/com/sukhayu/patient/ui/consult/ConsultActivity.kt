package com.sukhayu.patient.ui.consult

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.TtsHelper
import com.sukhayu.patient.utils.HeaderUtils

class ConsultActivity : AppCompatActivity() {

    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consult)
        HeaderUtils.setupRoleInHeader(this)
        ttsHelper = TtsHelper(this)
        ttsHelper.setLanguage("en")

        // TODO: Add TTS to consultation interface
    }

    override fun onDestroy() {
        ttsHelper.shutdown()
        super.onDestroy()
    }
}