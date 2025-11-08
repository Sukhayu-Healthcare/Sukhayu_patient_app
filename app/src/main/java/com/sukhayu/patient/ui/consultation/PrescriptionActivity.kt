package com.sukhayu.patient.ui.consultation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.databinding.ActivityPrescriptionBinding
// import com.sukhayu.patient.model.Medicine  // Commented
// import android.content.Intent
// import android.net.Uri

class PrescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrescriptionBinding
    // private lateinit var adapter: MedicineAdapter  // Commented

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // adapter = MedicineAdapter()
        // binding.rvMedicines.layoutManager = LinearLayoutManager(this)
        // binding.rvMedicines.adapter = adapter

        // loadPrescriptionData()

        // Disable the download button action for now
        binding.btnDownload.setOnClickListener {
            // val pdfUri = Uri.parse("https://example.com/prescription.pdf")
            // val intent = Intent(Intent.ACTION_VIEW, pdfUri)
            // startActivity(intent)
        }

        // Just set some dummy text for UI testing
        binding.tvDoctorName.text = "Dr. (Frontend Test Mode)"
        binding.tvDate.text = "Date: UI Preview Only"
    }

    /*
    private fun loadPrescriptionData() {
        val medicines = listOf(
            Medicine("Paracetamol 500mg", "1 tablet twice daily after meal"),
            Medicine("Azithromycin 250mg", "1 tablet daily for 3 days")
        )
        binding.tvDoctorName.text = "Dr. Anjali Sharma"
        binding.tvDate.text = "Date: 07 Nov 2025"
        adapter.submitList(medicines)
    }
    */
}
