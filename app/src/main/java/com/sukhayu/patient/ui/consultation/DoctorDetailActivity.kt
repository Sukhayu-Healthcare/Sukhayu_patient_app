// app/src/main/java/com/sukhayu/patient/ui/consultation/DoctorDetailActivity.kt
package com.sukhayu.patient.ui.consultation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.R
import com.sukhayu.utils.VoiceInputHelper
import com.sukhayu.patient.utils.HeaderUtils

class DoctorDetailActivity : AppCompatActivity() {

    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Simple minimal layout: we'll reuse a built-in layout if you don't have a dedicated XML.
        // Create a very small layout programmatically so there's no missing resource dependency.
        val tv = TextView(this).apply {
            textSize = 16f
            val name = intent.getStringExtra("doctor_name") ?: "Unknown Doctor"
            val spec = intent.getStringExtra("doctor_specialty") ?: ""
            val rating = intent.getDoubleExtra("doctor_rating", -1.0)
            text = "Doctor: $name\nSpecialty: $spec" + if (rating >= 0) "\nRating: $rating" else ""
            setPadding(32, 32, 32, 32)
        }
        setContentView(tv)

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
