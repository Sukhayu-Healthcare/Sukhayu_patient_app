package com.sukhayu.patient.ui.ai_symptom

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
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
            
            // Get layout params with proper type
            val params = binding.messageFrame.layoutParams as? LinearLayout.LayoutParams ?: LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            
            if (message.isUserMessage) {
                try {
                    binding.tvMessage.background = itemView.context.getDrawable(com.sukhayu.patient.R.drawable.bg_user_message)
                } catch (e: Exception) {
                    // Fallback if drawable not found
                }
                binding.tvMessage.setTextColor(Color.WHITE)
                params.gravity = android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
                binding.messageFrame.layoutParams = params
            } else {
                try {
                    binding.tvMessage.background = itemView.context.getDrawable(com.sukhayu.patient.R.drawable.bg_bot_message)
                } catch (e: Exception) {
                    // Fallback if drawable not found
                }
                binding.tvMessage.setTextColor(message.zoneColor ?: Color.parseColor("#1E293B"))
                params.gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
                binding.messageFrame.layoutParams = params
            }
        }
    }
}
