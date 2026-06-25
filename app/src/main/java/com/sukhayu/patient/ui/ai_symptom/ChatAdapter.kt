package com.sukhayu.patient.ui.ai_symptom

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.databinding.ChatMessageItemBinding

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ChatMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class ChatViewHolder(private val binding: ChatMessageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.text
            
            val params = binding.messageFrame.layoutParams as LinearLayout.LayoutParams
            
            if (message.isUserMessage) {
                // User Message: Green box on the Right
                binding.tvMessage.setBackgroundResource(R.drawable.bg_user_message)
                binding.tvMessage.setTextColor(Color.WHITE)
                params.gravity = Gravity.END
                
                // Set margins for better alignment
                params.setMargins(60, 4, 0, 4)
            } else {
                // AI Message: White/Grey box on the Left
                binding.tvMessage.setBackgroundResource(R.drawable.bg_bot_message)
                
                // Use zone color for text or default dark blue
                binding.tvMessage.setTextColor(message.zoneColor ?: Color.parseColor("#1E293B"))
                params.gravity = Gravity.START
                
                // Set margins for better alignment
                params.setMargins(0, 4, 60, 4)
            }
            
            binding.messageFrame.layoutParams = params
        }
    }
}
