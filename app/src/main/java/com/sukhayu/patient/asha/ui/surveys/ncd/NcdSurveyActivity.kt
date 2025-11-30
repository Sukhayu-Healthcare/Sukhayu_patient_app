package com.sukhayu.patient.asha.ui.surveys.ncd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.utils.VoiceInputHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Canonical NCD Survey Activity.
 * - Package must be: com.sukhayu.patient.asha.ui.surveys.ncd
 * - File name must be: NcdSurveyActivity.kt
 * - Class name must be: NcdSurveyActivity
 *
 * TODO: replace the placeholder onCreate implementation with the real UI when available.
 */
class NcdSurveyActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Intentionally not setting a layout here to avoid referencing missing resources.
        // When a layout exists, set it with: setContentView(R.layout.activity_ncd_survey)

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
}
