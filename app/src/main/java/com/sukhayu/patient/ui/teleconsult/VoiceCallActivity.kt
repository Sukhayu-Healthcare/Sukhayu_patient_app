package com.sukhayu.patient.ui.teleconsult

import android.Manifest
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.databinding.ActivityVoiceCallBinding
import com.sukhayu.utils.VoiceInputHelper
// import androidx.activity.viewModels
// import com.sukhayu.patient.viewmodel.TeleconsultViewModel

class VoiceCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceCallBinding
    // private val viewModel: TeleconsultViewModel by viewModels()  // Commented
    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // viewModel.startVoiceSession()

        binding.btnEndCall.setOnClickListener {
            // viewModel.endSession()
            finish()
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
        // viewModel.endSession()
        voiceHelper.destroy()
    }
}
