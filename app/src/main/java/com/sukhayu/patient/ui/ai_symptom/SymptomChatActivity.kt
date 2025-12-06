package com.sukhayu.patient.ui.ai_symptom

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.sukhayu.patient.databinding.ActivitySymptomChatBinding
import com.sukhayu.patient.viewmodel.SymptomViewModel

class SymptomChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySymptomChatBinding
    private val viewModel: SymptomViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySymptomChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            val text = binding.etComplaint.text.toString()
            if (text.isNotBlank()) {
                viewModel.updateComplaint(text)
                viewModel.sendComplaintOrFollowup()
            }
        }

        binding.btnSendFollowup.setOnClickListener {
            val answer = binding.etFollowupAnswer.text.toString()
            if (answer.isNotBlank()) viewModel.sendComplaintOrFollowup(answer)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                if (state.isLoading) {
                    binding.tvResult.text = "Loading..."
                    binding.tvResult.setTextColor(Color.GRAY)
                } else if (state.errorMessage != null) {
                    binding.tvResult.text = state.errorMessage
                    binding.tvResult.setTextColor(Color.RED)
                } else {
                    state.lastResponse?.let { res ->
                        val sb = StringBuilder()
                        sb.appendLine(res.zone_label)
                        sb.appendLine(res.patient_symptoms_line)
                        sb.appendLine("काय करावे:")
                        sb.appendLine(res.patient_action_line)
                        sb.appendLine("Follow-up: ${res.followup_question ?: "नाही"}")
                        binding.tvResult.text = sb.toString()

                        binding.tvResult.setTextColor(
                            when (res.zone) {
                                "Red" -> Color.RED
                                "Yellow" -> Color.YELLOW
                                "Green" -> Color.GREEN
                                else -> Color.GRAY
                            }
                        )
                    }
                }
            }
        }
    }
}
