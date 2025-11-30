package com.sukhayu.patient.ui.teleconsult

import android.Manifest
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.databinding.ActivityChatFallbackBinding
import com.sukhayu.utils.VoiceInputHelper

class ChatFallbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatFallbackBinding
    private val messages = mutableListOf<String>()
    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatFallbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)

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

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    private fun simulateDoctorReply() {
        binding.chatText.append("\nDoctor: Please continue describing your symptoms.")
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
