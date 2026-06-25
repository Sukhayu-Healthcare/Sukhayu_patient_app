package com.sukhayu.patient.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.utils.LocaleHelper

/**
 * Base activity class for all patient activities that support language localization.
 * Handles automatic language switching and persistence via SharedPreferences.
 * 
 * Usage:
 * class MyPatientActivity : LocalizableActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_my_patient)
 *         setupLanguageToggle()
 *     }
 * }
 */
abstract class LocalizableActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "settings"
        private const val LANG_KEY = "app_lang"
        private const val DEFAULT_LANG = "mr"  // Marathi as default
        
        val LANGUAGES = arrayOf("मराठी", "English", "हिन्दी", "ગુજરાती")
        val LANGUAGE_CODES = arrayOf("mr", "en", "hi", "gu")
    }

    // Apply saved locale before activity context is used
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lang = prefs.getString(LANG_KEY, DEFAULT_LANG) ?: DEFAULT_LANG
        val wrapped = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(wrapped)
    }

    /**
     * Get current language code (e.g., "mr", "en", "hi", "gu")
     */
    fun getCurrentLanguage(): String {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getString(LANG_KEY, DEFAULT_LANG) ?: DEFAULT_LANG
    }

    /**
     * Change language and recreate activity with new locale
     */
    fun changeLanguage(languageCode: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(LANG_KEY, languageCode).apply()
        recreate()
    }

    /**
     * Show language selection dialog
     */
    fun showLanguageDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("भाषा निवडा / Choose Language")
            .setItems(LANGUAGES) { _, which ->
                changeLanguage(LANGUAGE_CODES[which])
            }
            .show()
    }

    /**
     * Setup language toggle button in header (if available)
     * Call this in onCreate() after setContentView()
     */
    fun setupLanguageToggle() {
        val languageToggleBar = findViewById<android.widget.LinearLayout>(R.id.languageToggleBar)
        if (languageToggleBar != null) {
            // Clear any existing children
            languageToggleBar.removeAllViews()
            
            // Create translator button with text
            val translateButton = android.widget.Button(this).apply {
                text = "🌐 Language"
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setTextColor(android.graphics.Color.parseColor("#1E40AF"))
                textSize = 12f
                setPadding(12, 4, 12, 4)
                setOnClickListener {
                    showLanguageDialog()
                }
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 8
                }
            }
            
            languageToggleBar.addView(translateButton)
        }
    }
}
