package com.sukhayu.patient.ui.emergency

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sukhayu.patient.databinding.ActivityEmergencyInitBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmergencyInitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyInitBinding
    private var countdown = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyInitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            while (countdown > 0) {
                binding.tvCountdown.text = countdown.toString()
                delay(1000)
                countdown--
            }
            startActivity(Intent(this@EmergencyInitActivity, EmergencyVCActivity::class.java))
            finish()
        }
    }
}
