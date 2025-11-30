package com.sukhayu.patient

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.sukhayu.utils.MarathiTranslator

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize translator
        try {
            MarathiTranslator.getInstance(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
