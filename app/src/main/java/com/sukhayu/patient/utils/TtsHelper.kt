package com.sukhayu.patient.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TtsHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    var selectedLanguage: String = "en" // "en" or "mr"

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            updateLocale()
            Log.d("TtsHelper", "TTS initialized successfully")
        } else {
            Log.e("TtsHelper", "TTS initialization failed")
        }
    }

    /**
     * Updates the TTS locale based on selectedLanguage
     */
    private fun updateLocale() {
        tts?.let { ttsEngine ->

            val locale = when (selectedLanguage.lowercase()) {
                "mr" -> Locale("mr", "IN")  // Marathi
                "hi" -> Locale("hi", "IN")  // Hindi
                "gu" -> Locale("gu", "IN")  // Gujarati
                else -> Locale.US           // English
            }

            val result = ttsEngine.setLanguage(locale)

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {

                Log.w(
                    "TtsHelper",
                    "Language $selectedLanguage not supported. Falling back to English."
                )

                ttsEngine.setLanguage(Locale.US)
            } else {

                Log.d(
                    "TtsHelper",
                    "TTS language set to ${locale.displayLanguage}"
                )
            }
        }
    }

    /**
     * Speaks the given text. Automatically uses the current selectedLanguage.
     * @param text The text to speak
     */
    fun speak(text: String) {
        if (!isInitialized) {
            Log.w("TtsHelper", "TTS not initialized yet")
            return
        }
        
        if (text.isBlank()) {
            return
        }

        // Update locale in case language changed
        updateLocale()
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    /**
     * Stops current speech
     */
    fun stop() {
        tts?.stop()
    }

    /**
     * Checks if TTS is currently speaking
     */
    fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }

    /**
     * Sets the language for TTS
     * @param language "en" for English, "mr" for Marathi
     */
    fun setLanguage(language: String) {
        selectedLanguage = language
        updateLocale()
    }

    /**
     * Must be called in Activity/Fragment onDestroy
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        Log.d("TtsHelper", "TTS shutdown")
    }
}
