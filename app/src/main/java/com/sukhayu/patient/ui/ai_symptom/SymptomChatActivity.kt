package com.sukhayu.patient.ui.ai_symptom

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.databinding.ItemChatBotBinding
import com.sukhayu.patient.databinding.ItemChatUserBinding
import com.sukhayu.patient.databinding.ActivitySymptomChatBinding

class SymptomChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySymptomChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySymptomChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val inflater = LayoutInflater.from(this)

        // Back button working
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Send message on clicking send icon
        binding.btnSend.setOnClickListener {
            val text = binding.etSymptoms.text.toString().trim()
            if (text.isNotEmpty()) {
                addUserMessage(text)
                binding.etSymptoms.setText("")
                processBotReply(text)
            }
        }
    }

    private fun addUserMessage(text: String) {
        val inflater = LayoutInflater.from(this)
        val userBubble = ItemChatUserBinding.inflate(inflater)

        userBubble.tvUserMessage.text = text

        binding.chatContainer.addView(userBubble.root)
        scrollToBottom()
    }

    private fun addBotMessage(text: String) {
        val inflater = LayoutInflater.from(this)
        val botBubble = ItemChatBotBinding.inflate(inflater)

        botBubble.tvBotMessage.text = text

        binding.chatContainer.addView(botBubble.root)
        scrollToBottom()
    }

    private fun processBotReply(input: String) {
        val basicReply = SymptomChecker.analyzeSymptoms(input)
        val ruleReply = SymptomRules.inferDisease(input)

        val finalReply = "$basicReply\n\n$ruleReply"

        addBotMessage(finalReply)
    }

    private fun scrollToBottom() {
        binding.scrollArea.post {
            binding.scrollArea.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}
