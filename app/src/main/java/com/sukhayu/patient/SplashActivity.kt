package com.sukhayu.patient

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.utils.TtsHelper

class SplashActivity : AppCompatActivity() {

    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ttsHelper = TtsHelper(this)
        ttsHelper.setLanguage("en")
    }

    override fun onDestroy() {
        ttsHelper.shutdown()
        super.onDestroy()
    }
}