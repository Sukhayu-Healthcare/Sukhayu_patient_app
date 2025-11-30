package com.sukhayu.patient.ui.consultation

import android.Manifest
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.databinding.ActivityPrescriptionBinding
import com.sukhayu.utils.VoiceInputHelper

class PrescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrescriptionBinding
    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)

        binding.btnDownload.setOnClickListener {
            // val pdfUri = Uri.parse("https://example.com/prescription.pdf")
            // val intent = Intent(Intent.ACTION_VIEW, pdfUri)
            // startActivity(intent)
        }

        // Just set some dummy text for UI testing
        binding.tvDoctorName.text = "Dr. (Frontend Test Mode)"
        binding.tvDate.text = "Date: UI Preview Only"
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
