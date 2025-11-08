package com.sukhayu.patient.ui.teleconsult

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.databinding.ActivityVoiceCallBinding
// import androidx.activity.viewModels
// import com.sukhayu.patient.viewmodel.TeleconsultViewModel

class VoiceCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceCallBinding
    // private val viewModel: TeleconsultViewModel by viewModels()  // Commented

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // viewModel.startVoiceSession()

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
