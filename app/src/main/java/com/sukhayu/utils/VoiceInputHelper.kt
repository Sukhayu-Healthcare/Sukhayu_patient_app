package com.sukhayu.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.sukhayu.patient.R

class VoiceInputHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var currentEditText: EditText? = null
    private val translator = MarathiTranslator.getInstance(context)

    fun attachVoiceToEditText(editText: EditText, language: String? = null) {
        if (isPasswordField(editText)) return

        try {
            val micIcon = ContextCompat.getDrawable(context, R.drawable.ic_mic)
            if (micIcon == null) {
                android.util.Log.e("VoiceInputHelper", "Mic icon not found")
                return
            }
            
            micIcon.setBounds(0, 0, micIcon.intrinsicWidth, micIcon.intrinsicHeight)
            
            editText.setCompoundDrawablesWithIntrinsicBounds(
                editText.compoundDrawables[0],
                editText.compoundDrawables[1],
                micIcon,
                editText.compoundDrawables[3]
            )

            editText.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val drawableEnd = 2
                    editText.compoundDrawables[drawableEnd]?.let { drawable ->
                        if (event.rawX >= (editText.right - drawable.bounds.width() - editText.paddingEnd)) {
                            startVoiceInput(editText, language)
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("VoiceInputHelper", "Error attaching voice to EditText", e)
        }
    }

    fun startVoiceInput(editText: EditText, language: String? = null) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
            return
        }

        currentEditText = editText
        
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language ?: "en-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "mr-IN")
            putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("en-IN", "mr-IN"))
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(context, "Listening...", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Recognition error"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    processVoiceResult(spokenText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun processVoiceResult(text: String) {
        val finalText = if (isMarathi(text)) {
            translator.translateMarathiToEnglish(text)
        } else {
            text
        }
        
        currentEditText?.setText(finalText)
        Toast.makeText(context, "Text entered: $finalText", Toast.LENGTH_SHORT).show()
    }

    fun isMarathi(text: String): Boolean {
        val marathiPattern = Regex("[\u0900-\u097F]+")
        return marathiPattern.containsMatchIn(text)
    }

    private fun isPasswordField(editText: EditText): Boolean {
        val inputType = editText.inputType
        return (inputType and 0x00000090) != 0 || (inputType and 0x00000010) != 0
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    companion object {
        fun attachToAllEditTexts(activity: Activity) {
            try {
                val helper = VoiceInputHelper(activity)
                val rootView = activity.window.decorView.rootView
                attachToViewGroup(rootView, helper)
            } catch (e: Exception) {
                android.util.Log.e("VoiceInputHelper", "Error attaching to all EditTexts", e)
            }
        }

        private fun attachToViewGroup(view: android.view.View, helper: VoiceInputHelper) {
            if (view is EditText) {
                helper.attachVoiceToEditText(view)
            } else if (view is android.view.ViewGroup) {
                for (i in 0 until view.childCount) {
                    attachToViewGroup(view.getChildAt(i), helper)
                }
            }
        }
    }
}
