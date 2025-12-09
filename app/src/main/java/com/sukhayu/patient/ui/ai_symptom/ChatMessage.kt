package com.sukhayu.patient.ui.ai_symptom

data class ChatMessage(
    val text: String,
    val isUserMessage: Boolean,
    val zoneColor: Int? = null
)
