package com.sukhayu.patient.ui.teleconsult

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.databinding.ActivityConsentBinding
import com.sukhayu.utils.VoiceInputHelper

class ConsentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsentBinding
    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)

        // Return RESULT_OK to the caller when user accepts instead of starting VideoCallActivity here.
        binding.btnAccept.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        binding.btnDecline.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
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
