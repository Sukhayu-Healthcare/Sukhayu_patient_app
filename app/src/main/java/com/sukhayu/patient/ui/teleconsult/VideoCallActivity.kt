package com.sukhayu.patient.ui.teleconsult

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.databinding.ActivityVideoCallBinding
// import androidx.activity.viewModels
// import com.sukhayu.patient.viewmodel.TeleconsultViewModel

class VideoCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoCallBinding
    // private val viewModel: TeleconsultViewModel by viewModels()  // Commented

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // viewModel.startVideoSession()

        binding.btnEndCall.setOnClickListener {
            // viewModel.endSession()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // viewModel.endSession()
    }
}
