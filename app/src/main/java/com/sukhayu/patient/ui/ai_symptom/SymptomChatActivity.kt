package com.sukhayu.patient.ui.ai_symptom

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sukhayu.patient.databinding.ActivitySymptomChatBinding
import com.sukhayu.patient.viewmodel.SymptomViewModel
import kotlinx.coroutines.launch

class SymptomChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySymptomChatBinding
    private val viewModel: SymptomViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivitySymptomChatBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Setup RecyclerView
            chatAdapter = ChatAdapter()
            binding.rvChat.apply {
                layoutManager = LinearLayoutManager(this@SymptomChatActivity)
                adapter = chatAdapter
            }

            // Add initial greeting
            chatAdapter.addMessage(ChatMessage("Hello! I'm your AI assistant. Describe your symptoms.", false))

            // Back button
            binding.backButton.setOnClickListener {
                finish()
            }

            // Make send button clickable
            binding.btnSend.setOnClickListener {
                val text = binding.etComplaint.text.toString().trim()
                if (text.isNotEmpty()) {
                    // Add user message to chat
                    chatAdapter.addMessage(ChatMessage(text, true))
                    binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                    
                    viewModel.updateComplaint(text)
                    viewModel.sendComplaintOrFollowup()
                    
                    // Clear input
                    binding.etComplaint.text.clear()
                }
            }

            binding.btnSendFollowup.setOnClickListener {
                val answer = binding.etFollowupAnswer.text.toString().trim()
                if (answer.isNotEmpty()) {
                    // Add user message to chat
                    chatAdapter.addMessage(ChatMessage(answer, true))
                    binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                    
                    viewModel.sendComplaintOrFollowup(answer)
                    
                    // Clear input
                    binding.etFollowupAnswer.text.clear()
                }
            }

            // Collect UI state
            lifecycleScope.launch {
                try {
                    viewModel.uiState.collect { state ->
                        try {
                            if (state.isLoading) {
                                // Add loading indicator
                                chatAdapter.addMessage(ChatMessage("Analyzing your symptoms...", false))
                                binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                            } else if (!state.errorMessage.isNullOrEmpty()) {
                                // Show error message
                                chatAdapter.addMessage(ChatMessage("❌ Error: ${state.errorMessage}", false))
                                binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                            } else if (state.lastResponse != null) {
                                // Show response
                                val res = state.lastResponse
                                val sb = StringBuilder()
                                sb.append(res.zone_label).append("\n")
                                sb.append(res.patient_symptoms_line).append("\n")
                                sb.append("काय करावे:\n")
                                sb.append(res.patient_action_line)
                                if (!res.followup_question.isNullOrEmpty()) {
                                    sb.append("\n\nQuestion: ").append(res.followup_question)
                                }
                                
                                val zoneColor = when (res.zone) {
                                    "Red" -> Color.RED
                                    "Yellow" -> Color.YELLOW
                                    "Green" -> Color.GREEN
                                    else -> Color.GRAY
                                }
                                
                                chatAdapter.addMessage(ChatMessage(sb.toString(), false, zoneColor))
                                binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    chatAdapter.addMessage(ChatMessage("Error loading chat: ${e.localizedMessage}", false))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
