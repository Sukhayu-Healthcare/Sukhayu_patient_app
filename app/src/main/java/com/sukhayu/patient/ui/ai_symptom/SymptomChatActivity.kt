package com.sukhayu.patient.ui.ai_symptom

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sukhayu.patient.databinding.ActivitySymptomChatBinding
import com.sukhayu.patient.utils.HeaderUtils
import com.sukhayu.patient.utils.LocalizableActivity
import com.sukhayu.patient.viewmodel.SymptomViewModel
import com.sukhayu.utils.VoiceInputHelper
import kotlinx.coroutines.launch
import android.view.View
import android.widget.AdapterView
import com.sukhayu.patient.utils.TtsHelper
import com.sukhayu.patient.utils.ViewTtsHelper

class SymptomChatActivity : LocalizableActivity(){

    private lateinit var binding: ActivitySymptomChatBinding
    private val viewModel: SymptomViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var voiceHelper: VoiceInputHelper

    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivitySymptomChatBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Setup language toggle from header
            setupLanguageToggle()
            HeaderUtils.setupRoleInHeader(this)
            // Setup RecyclerView
            chatAdapter = ChatAdapter()
            binding.rvChat.apply {
                layoutManager = LinearLayoutManager(this@SymptomChatActivity)
                adapter = chatAdapter
            }

            // Initial greeting
            chatAdapter.addMessage(
                ChatMessage(
                    "Hello! I'm your AI assistant. Describe your symptoms.",
                    false
                )
            )

            // Voice input setup
            requestAudioPermission()
            voiceHelper = VoiceInputHelper(this)
            VoiceInputHelper.attachToAllEditTexts(this)

            // Back button
            binding.backButton.setOnClickListener {
                finish()
            }

            // Send button
            binding.btnSend.setOnClickListener {
                val text = binding.etComplaint.text.toString().trim()

                if (text.isNotEmpty()) {
                    chatAdapter.addMessage(ChatMessage(text, true))
                    binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)

                    viewModel.updateComplaint(text)
                    viewModel.sendComplaintOrFollowup()

                    binding.etComplaint.text.clear()
                }
            }

            // Observe ViewModel state
            lifecycleScope.launch {
                viewModel.uiState.collect { state ->

                    try {
                        when {
                            state.isLoading -> {
                                chatAdapter.addMessage(
                                    ChatMessage(
                                        "Analyzing your symptoms...",
                                        false
                                    )
                                )
                                binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                            }

                            !state.errorMessage.isNullOrEmpty() -> {
                                chatAdapter.addMessage(
                                    ChatMessage(
                                        "❌ Error: ${state.errorMessage}",
                                        false
                                    )
                                )
                                binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                            }

                            state.lastResponse != null -> {
                                val res = state.lastResponse

                                val responseText = buildString {
                                    append(res.zone_label)
                                    append("\n")
                                    append(res.patient_symptoms_line)
                                    append("\n")
                                    append("काय करावे:\n")
                                    append(res.patient_action_line)

                                    if (!res.followup_question.isNullOrEmpty()) {
                                        append("\n\nQuestion: ")
                                        append(res.followup_question)
                                    }
                                }

                                val zoneColor = when (res.zone) {
                                    "Red" -> Color.RED
                                    "Yellow" -> Color.YELLOW
                                    "Green" -> Color.GREEN
                                    else -> Color.GRAY
                                }

                                chatAdapter.addMessage(
                                    ChatMessage(
                                        responseText,
                                        false,
                                        zoneColor
                                    )
                                )

                                binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Initialize TTS
        ttsHelper = TtsHelper(this)

        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val currentLang = prefs.getString("My_Lang", "en") ?: "en"

        ttsHelper.setLanguage(currentLang)

        // Enable TTS on all TextViews and Buttons
        ViewTtsHelper.attachToAllTextViews(
            findViewById(android.R.id.content),
            ttsHelper
        )
    }

    private fun requestAudioPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::voiceHelper.isInitialized) {
            voiceHelper.destroy()
        }
    }
}