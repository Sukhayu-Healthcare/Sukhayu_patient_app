package com.sukhayu.patient.ui.teleconsult

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.databinding.ActivityChatFallbackBinding

class ChatFallbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatFallbackBinding
    private val messages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatFallbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            val msg = binding.etMessage.text.toString()
            if (msg.isNotEmpty()) {
                messages.add("You: $msg")
                binding.etMessage.text.clear()
                binding.chatText.append("\n$msg")
                simulateDoctorReply()
            }
        }
    }

    private fun simulateDoctorReply() {
        binding.chatText.append("\nDoctor: Please continue describing your symptoms.")
    }
}
