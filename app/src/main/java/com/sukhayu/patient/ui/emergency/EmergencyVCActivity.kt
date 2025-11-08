package com.sukhayu.patient.ui.emergency

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.databinding.ActivityEmergencyVcBinding

class EmergencyVCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyVcBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyVcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEndEmergency.setOnClickListener {
            finish()
        }
    }
}
