package com.sukhayu.patient.ui.ai_symptom

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sukhayu.patient.R
import com.sukhayu.patient.viewmodel.SymptomViewModel
import kotlinx.coroutines.flow.collect
import androidx.activity.viewModels

class CheckSymptomsActivity : AppCompatActivity() {

    private val viewModel: SymptomViewModel by viewModels()

    private lateinit var etSymptoms: EditText
    private lateinit var btnMic: ImageView
    private lateinit var btnSend: ImageView
    private lateinit var etFollowupAnswer: EditText
    private lateinit var btnSendFollowup: Button
    private lateinit var chatContainer: LinearLayout
    private lateinit var scrollArea: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_symptoms)

        // Bind views
        etSymptoms = findViewById(R.id.etSymptoms)
        btnMic = findViewById(R.id.btnMic)
        btnSend = findViewById(R.id.btnSend)
        etFollowupAnswer = findViewById(R.id.etFollowupAnswer)
        btnSendFollowup = findViewById(R.id.btnSendFollowup)
        chatContainer = findViewById(R.id.chatContainer)
        scrollArea = findViewById(R.id.scrollArea)

        btnSend.setOnClickListener {
            val text = etSymptoms.text.toString()
            if (text.isNotBlank()) {
                addUserMessage(text)
                etSymptoms.text.clear()
                viewModel.sendComplaintOrFollowup(text)
            }
        }

        btnSendFollowup.setOnClickListener {
            val answer = etFollowupAnswer.text.toString()
            if (answer.isNotBlank()) {
                addUserMessage(answer)
                etFollowupAnswer.text.clear()
                viewModel.sendComplaintOrFollowup(answer)
            }
        }

        // Observe ViewModel state
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                state.lastResponse?.let { res ->
                    addBotMessage(
                        "${res.zone_label}\n${res.patient_symptoms_line}\nकाय करावे:\n${res.patient_action_line}\nFollow-up: ${res.followup_question ?: "नाही"}",
                        res.zone
                    )
                }
            }
        }
    }

    private fun addUserMessage(message: String) {
        val textView = TextView(this)
        textView.text = message
        textView.setBackgroundResource(R.drawable.bg_user_message)
        textView.setTextColor(Color.WHITE)
        textView.setPadding(20, 16, 20, 16)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.marginStart = 100
        params.topMargin = 8
        textView.layoutParams = params
        chatContainer.addView(textView)
        scrollToBottom()
    }

    private fun addBotMessage(message: String, zone: String?) {
        val textView = TextView(this)
        textView.text = message
        textView.setBackgroundResource(R.drawable.bg_bot_message)
        textView.setTextColor(
            when (zone) {
                "Red" -> Color.RED
                "Yellow" -> Color.YELLOW
                "Green" -> Color.GREEN
                else -> Color.GRAY
            }
        )
        textView.setPadding(20, 16, 20, 16)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.marginEnd = 100
        params.topMargin = 8
        textView.layoutParams = params
        chatContainer.addView(textView)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        scrollArea.post {
            scrollArea.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    companion object {
    const val EXTRA_SYMPTOM_TEXT = "extra_symptom_text"
    const val EXTRA_SYMPTOM_ANALYSIS = "extra_symptom_analysis"
}

}
