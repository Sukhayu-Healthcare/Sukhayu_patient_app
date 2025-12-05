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
import java.util.Locale

class VoiceInputHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var currentEditText: EditText? = null
    private val translator = MarathiTranslator.getInstance(context)
    private var currentLanguage: String? = null

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
        // Keep currentLanguage as null when caller didn't request a specific language
        currentLanguage = language

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }

        // If language param is provided (e.g. "mr-IN" or "en-IN"), request that language explicitly.
        // If language param is null, do NOT set EXTRA_LANGUAGE so Android's inbuilt recognizer
        // can choose the best language automatically (device/default / user selection).
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            if (!language.isNullOrBlank()) {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
                // Optionally hint supported languages when explicit language requested
                putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("en-IN", "mr-IN"))
            } else {
                // Let Android choose/detect language (use inbuilt multi-language support)
            }
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                val langLabel = when {
                    currentLanguage == "mr-IN" -> "Marathi"
                    currentLanguage == "en-IN" -> "English"
                    else -> "Detecting"
                }
                Toast.makeText(context, "Listening ($langLabel)...", Toast.LENGTH_SHORT).show()
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
        // Detect language using improved detector
        val detected = detectLanguage(text)
        val originalText = text
        val finalText: String
        val toastMessage: String

        when {
            currentLanguage == "mr-IN" -> {
                // Caller explicitly requested Marathi — keep Marathi text as-is
                finalText = originalText
                toastMessage = "Detected: Marathi\nRecognized: $originalText"
            }
            detected == "mr" -> {
                // Marathi speech detected — show Marathi only (do not translate)
                finalText = originalText
                toastMessage = "Detected: Marathi\nRecognized: $originalText"
            }
            else -> {
                // English or other detected — keep as-is
                finalText = originalText
                toastMessage = "Detected: English\nRecognized: $originalText"
            }
        }

        currentEditText?.setText(finalText)
        Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
    }

    /**
     * Detects whether given text is Marathi or English.
     * Returns "mr" for Marathi, "en" for English.
     *
     * Strategy:
     *  - Count Devanagari characters and alphabetic letters; compute ratio.
     *  - Check for a small list of common Marathi words as a heuristic.
     *  - If Devanagari ratio is significant or common Marathi words are found => Marathi.
     */
    fun detectLanguage(text: String): String {
        if (text.isBlank()) return "en"

        val devanagariRegex = Regex("[\u0900-\u097F]")
        val letterRegex = Regex("\\p{L}")

        val devanagariCount = devanagariRegex.findAll(text).count()
        val lettersCount = letterRegex.findAll(text).count().coerceAtLeast(1)

        val ratio = devanagariCount.toDouble() / lettersCount.toDouble()

        val commonMarathiWords = setOf(
            "आहे", "नाही", "हो", "काय", "मला", "तुमचा", "तुमच्या", "कृपया", "धन्यवाद", "बघा", "आहेत"
        )
        val lower = text.toLowerCase(Locale.getDefault())
        val containsCommon = commonMarathiWords.any { lower.contains(it) }

        // Heuristic thresholds:
        // - If any Devanagari present and ratio is > 30% OR common Marathi words are found => Marathi
        return if (devanagariCount > 0 && (ratio > 0.30 || containsCommon)) "mr" else "en"
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
        // Keep map of helpers per activity so we can destroy them later
        private val helpers = mutableMapOf<Activity, VoiceInputHelper>()

        fun attachToAllEditTexts(activity: Activity) {
            try {
                // If already attached for this activity, skip re-attaching
                if (helpers.containsKey(activity)) return
                val helper = VoiceInputHelper(activity)
                helpers[activity] = helper
                val rootView = activity.window.decorView.rootView
                attachToViewGroup(rootView, helper)
            } catch (e: Exception) {
                android.util.Log.e("VoiceInputHelper", "Error attaching to all EditTexts", e)
            }
        }

        /**
         * Destroy and remove helper for the given activity to free SpeechRecognizer resources.
         */
        fun destroyForActivity(activity: Activity) {
            try {
                helpers.remove(activity)?.destroy()
            } catch (e: Exception) {
                android.util.Log.e("VoiceInputHelper", "Error destroying helper for activity", e)
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