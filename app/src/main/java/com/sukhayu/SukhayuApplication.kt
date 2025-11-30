package com.sukhayu

import android.app.Application
import android.util.Log
import com.sukhayu.utils.MarathiTranslator

class SukhayuApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Marathi translator singleton
            MarathiTranslator.getInstance(this)
            Log.d("SukhayuApplication", "MarathiTranslator initialized successfully")
        } catch (e: Exception) {
            Log.e("SukhayuApplication", "Error initializing MarathiTranslator", e)
        }
    }
}