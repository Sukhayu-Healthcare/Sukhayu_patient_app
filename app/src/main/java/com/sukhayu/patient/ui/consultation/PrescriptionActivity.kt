package com.sukhayu.patient.ui.consultation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.databinding.ActivityPrescriptionBinding




class PrescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrescriptionBinding
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDownload.setOnClickListener {
            // val pdfUri = Uri.parse("https://example.com/prescription.pdf")
            // val intent = Intent(Intent.ACTION_VIEW, pdfUri)
            // startActivity(intent)
        }

        // Just set some dummy text for UI testing
        binding.tvDoctorName.text = "Dr. (Frontend Test Mode)"
        binding.tvDate.text = "Date: UI Preview Only"
    }
}
