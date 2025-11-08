package com.sukhayu.patient.ui.ai_symptom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sukhayu.patient.databinding.ActivitySymptomChatBinding

class SymptomChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySymptomChatBinding
    private lateinit var adapter: SymptomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySymptomChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SymptomAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // --- Dummy frontend data only (backend pending) ---
        val dummyMessages = listOf(
            ChatMessage("I have a headache and fever.", true),   // user message
            ChatMessage("You may have a mild viral infection. Stay hydrated.", false) // bot message
        )

        adapter.submitList(dummyMessages)
    }
}
