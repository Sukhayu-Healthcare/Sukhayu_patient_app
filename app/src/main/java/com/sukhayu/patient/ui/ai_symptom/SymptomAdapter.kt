package com.sukhayu.patient.ui.ai_symptom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.databinding.ItemChatBotBinding
import com.sukhayu.patient.databinding.ItemChatUserBinding

// Removed duplicate data class definition
// Fixed property names to match XML layout ids
class SymptomAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isUserMessage) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 1) {
            val binding = ItemChatUserBinding.inflate(inflater, parent, false)
            UserViewHolder(binding)
        } else {
            val binding = ItemChatBotBinding.inflate(inflater, parent, false)
            BotViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(message.text)
            is BotViewHolder -> holder.bind(message.text)
        }
    }

    class UserViewHolder(private val binding: ItemChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.tvUserMessage.text = text
        }
    }

    class BotViewHolder(private val binding: ItemChatBotBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.tvBotMessage.text = text
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem == newItem
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem.text == newItem.text
    }
}
